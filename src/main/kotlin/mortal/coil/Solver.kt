package mortal.coil

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.LinkedBlockingDeque
import kotlin.math.abs

class Solver(
    val height: Int,
    val width: Int,
    mapStr: String,
    val debug: Boolean = true,
    val parallelNum: Int,
    parent: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val scope = parent.plus(CoroutineName("Solver"))
    private var finished = false

    companion object {
        val directions = mapOf(
            'U' to Pair(-1, 0),
            'D' to Pair(1, 0),
            'L' to Pair(0, -1),
            'R' to Pair(0, 1),
        )

        const val WALL = -1
        const val EMPTY = 0
        const val PASS = 1
    }

    var remaining = 0
    var parallel = false
//    var startCount = AtomicInteger(0)
    var stepCount = 0

    private val map = Array(height) { h ->
        IntArray(width) { w ->
            if (mapStr[h * width + w] == 'X') {
                -1
            } else {
                remaining++
                0
            }
        }
    }
    val degreeMap = Array(height) { h ->
        IntArray(width) { w ->
            if (map[h][w] == EMPTY) calculateDegree(map, h to w)
            else 0
        }
    }

    val degreeArr = Array(5) {
        arrayListOf<Pair<Int, Int>>()
    }

    init {
        prepare()
        if (debug) {
            val sb = StringBuilder("\n")
            draw(height, width, map, debug, sb)
            drawDegree(height, width, degreeMap, debug, sb)
            log.info(sb.toString())
        }
    }

    private fun prepare() {
        for (i in 0 until height) {
            for (j in 0 until width) {
                val degree = degreeMap[i][j]
                degreeArr[degree].add(i to j)
            }
        }
    }

    fun solve(): Pair<Root, Node>? {
        for (i in 1 .. 4) {
            for ((h, w) in degreeArr[i]) {
                if (map[h][w] == 0) {
                    val point = h to w
//                    startCount.incrementAndGet()
                    val m = map.deepClone()
                    val d = degreeMap.deepClone()
                    val root = Root(m, d, point, remaining)
                    val node = solveNode(root) ?: continue
                    return root to node
                }
            }
        }

//        for (i in 0 until height) {
//            for (j in 0 until width) {
//                if (map[i][j] == 0) {
//                    startCount++
//                    val m = map.deepClone()
//                    val d = degreeMap.deepClone()
//                    val root = Root(m, d, i to j, remaining)
//                    val node = solveNode(root) ?: continue
//                    return root to node
//                }
//            }
//        }
        return null
    }

    suspend fun solveParallel(): Pair<Root, Node>? {
        parallel = true
        val channel = Channel<Pair<Root, Node?>>(BUFFERED)

        var count = 0
        for (i in 1 .. 4) {
            for ((h, w) in degreeArr[i]) {
                if (map[h][w] == 0) {
                    val point = h to w
                    val m = map.deepClone()
                    val d = degreeMap.deepClone()
                    val root = Root(m, d, point, remaining)
                    count++
                    scope.launch {
                        channel.send(root to solveNode(root))
                    }
                }
            }
        }
//        for (i in 0 until height) {
//            for (j in 0 until width) {
//                if (map[i][j] == 0) {
//                    scope.launch {
//                        val m = map.deepClone()
//                        val d = degreeMap.deepClone()
//                        val root = Root(m, d, i to j, remaining)
//                        channel.send(root to solveNode(root))
//                    }
//                }
//            }
//        }
        while (count > 0) {
            val receive = channel.receive()
            stepCount++
            val node = receive.second
            if (node != null && node.remaining == 0) {
                finished = true
                @Suppress("UNCHECKED_CAST")
                return receive as Pair<Root, Node>
            }
            count--
        }
        return null
    }

    private fun solveNode(root: Root): Node? {
        root.map[root.start.first][root.start.second] = 1
        degreeUpdate(root.map, root.start, root.degreeMap)
        root.remaining--

        val stack = LinkedBlockingDeque<Node>(height * width)
        stack.add(root.asNode())
        while (stack.isNotEmpty() && !finished) {
            val node = stack.pop()
            for ((direct, d) in directions) {
                if (
                    direct == node.latestDirect ||
                    node.latestDirect == 'U' && direct == 'D' ||
                    node.latestDirect == 'D' && direct == 'U' ||
                    node.latestDirect == 'L' && direct == 'R' ||
                    node.latestDirect == 'R' && direct == 'L'
                ) continue

                val next = node.next(direct, d) ?: continue

                // go straight until wall
                var n = next.curr
                while (validPoint(n) && next.degreeMap.at(n) > 0) {
                    next.map[n.first][n.second] = 1
                    degreeUpdate(next.map, n, next.degreeMap)
                    next.remaining--
                    next.curr = n
                    n += d
                }


                draw(this, root, next, debug)

                if (next.remaining == 0) return next
                if (next.remaining < 0) throw IllegalStateException("remaining < 0")

                if (check(next)) stack.push(next)

            }
        }

        return null
    }

    fun distance(a: Pair<Int, Int>, b: Pair<Int, Int>): Int {
        return abs(a.first - b.first) + abs(a.second - b.second)
    }

    fun check(
        map: Array<IntArray>,
        remaining: Int,
        cur: Pair<Int, Int>,
        degreeMap: Array<IntArray>
    ): Boolean {
        var s = 0
        var n = 1;
        var start: Pair<Int, Int> = 0 to 0
        val queue = ArrayBlockingQueue<Pair<Int, Int>>(width * height)
        for (i in 0 until height) {
            for (j in 0 until width) {
                if (map[i][j] == 0) {
                    start = i to j
                    break
                }
            }
            if (map[start.first][start.second] == 0) {
                break
            }
        }
        queue.add(start)

        val m = map.deepClone()
        m[start.first][start.second] = 1
        val p = if (distance(start, cur) == 1) 1 else 0
        if (degreeMap.at(start) + p == 1) {
            s++
        }

        while (!queue.isEmpty()) {
            val c = queue.poll()
            for ((_, d) in directions) {
                val next = c.first + d.first to c.second + d.second
                if (valid(m, next)) {
                    m[next.first][next.second] = 1
                    queue.add(next)
                    n++
                    if (validPoint(next) && degreeMap[next.first][next.second] + p == 1) {
                        s++
                        if (s > 1) {
                            return false
                        }
                    }
                }
            }
        }
        return n == remaining
    }

    private fun check(node: Node): Boolean {
        val m = node.map.deepClone()
        val degreeMap = node.degreeMap

        var endPointCount = 0
        lateinit var start: Pair<Int, Int>
        loop@ for (i in 0 until height) {
            for (j in 0 until width) {
                val p = i to j
                if (validPoint(p) && m.at(p) == 0) {
                    start = p
                    m[start.first][start.second] = 1
                    break@loop
                }
            }
        }

        if (degreeMap.at(start) + distance(start, node.curr) == 1) endPointCount++

        val q = LinkedBlockingDeque<Pair<Int, Int>>(node.remaining)
        q.push(start)

        var r = 1
        while (q.isNotEmpty()) {
            val p = q.pop()
            for ((_, d) in directions) {
                val n = p + d
                if (validPoint(n) && m.at(n) == EMPTY) {
                    if (degreeMap.at(n) + distance(n, node.curr) == 1) {
                        endPointCount++
                        if (endPointCount > 1) {
                            return false
                        }
                    }

                    m[n.first][n.second] = 1
                    q.push(n)
                    r++
                }
            }
        }

        return r==node.remaining
    }

    private fun calculateDegree(map: Array<IntArray>, cur: Pair<Int, Int>): Int {
        var n = 0
        for (vector in directions.values) {
            val next = cur + vector
            if (validPoint(next) && map.at(next) == EMPTY) {
                n++
            }
        }
        return n
    }

    fun degreeUpdate(map: Array<IntArray>, cur: Pair<Int, Int>, degreeMap: Array<IntArray>) {
        if (map[cur.first][cur.second] == EMPTY) {
            for ((_, d) in directions) {
                val next = cur + d
                if (validPoint(next) && map[next.first][next.second] == 0) {
                    degreeMap[next.first][next.second] = calculateDegree(map, next)
                }
            }
            degreeMap[cur.first][cur.second] = calculateDegree(map, cur)
        } else {
            degreeMap[cur.first][cur.second] = 0
        }
    }

    private fun validPoint(x: Int, y: Int): Boolean {
        return x in 0..<height && y in 0..<width
    }

    private fun validPoint(cur: Pair<Int, Int>): Boolean {
        return cur.first in 0..<height && cur.second in 0..<width
    }

    private fun valid(m: Array<IntArray>, cur: Pair<Int, Int>): Boolean {
        return validPoint(cur) && m.at(cur) == EMPTY
    }

    inner class Node(
        val map: Array<IntArray>,
        val degreeMap: Array<IntArray>,
        var curr: Pair<Int, Int>,
        var remaining: Int,
        var path: String,
    ) {
        val latestDirect = if (path.isNotEmpty()) path.last() else ' '

        fun next(dChar: Char, direction: Pair<Int, Int>): Node? {
            val next = curr + direction
            return if (validPoint(next).not() || degreeMap.at(next) <= 0) null
            else Node(map.deepClone(), degreeMap.deepClone(), next, remaining, path + dChar)
        }

        override fun toString(): String {
            return "Node(curr=$curr, remaining=$remaining, path='$path')"
        }
    }

    inner class Root(
        val map: Array<IntArray>,
        val degreeMap: Array<IntArray>,
        val start: Pair<Int, Int>,
        var remaining: Int
    ) {
        fun asNode(): Node {
            return Node(map, degreeMap, start, remaining, "")
        }

        override fun toString(): String {
            return "Root(start=$start, remaining=$remaining)"
        }


    }

}