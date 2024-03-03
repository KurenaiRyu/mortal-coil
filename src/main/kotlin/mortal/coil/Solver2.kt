//package mortal.coil
//
//import kotlinx.coroutines.*
//import kotlinx.coroutines.channels.Channel
//import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
//import java.util.concurrent.*
//import java.util.concurrent.atomic.AtomicInteger
//import kotlin.math.abs
//
//
//const val threadNum = 6
//val directions = mapOf(
//    'U' to Pair(-1, 0),
//    'D' to Pair(1, 0),
//    'L' to Pair(0, -1),
//    'R' to Pair(0, 1),
//)
//
//suspend fun solve(height: Int, width: Int, mapstr: String): String? {
//    val map = Array(height) { IntArray(width) }
//    var remaining = 0
//
//    for (i in 0 until height) {
//        for (j in 0 until width) {
//            if (mapstr[i * width + j] == 'X') {
//                map[i][j] = -1
//            } else {
//                map[i][j] = 0
//                remaining++
//            }
//        }
//    }
//
//    draw(height, width, map)
////    val drawStr = draw(height, width, map, false)
//
//    val degreemap = Array(height) { IntArray(width) }
//
//    for (i in 0 until height) {
//        for (j in 0 until width) {
//            if (map[i][j] == 0) {
//                degreemap[i][j] = degree(height, width, map, i to j)
//            } else {
//                degreemap[i][j] = 0
//            }
//        }
//    }
//
//    //solve
//    return solve(height, width, map, degreemap, remaining)
////    return solveParallel(height, width, map, degreemap, remaining)
//}
//
//private fun solve(
//    height: Int,
//    width: Int,
//    map: Array<IntArray>,
//    degreemap: Array<IntArray>,
//    remaining: Int
//): String? {
//    for (i in 0 until height) {
//        for (j in 0 until width) {
//            if (map[i][j] == 0) {
//                println("start $i $j")
//                val m = map.fullClone()
//                val d = degreemap.fullClone()
//                val path = singleSolve(height, width, m, remaining, i to j, i to j, "", d)
//                if (path != "") {
//                    return "($i $j) $path"
//                }
//            }
//        }
//    }
//    return null
//}
//
//@OptIn(DelicateCoroutinesApi::class)
//private suspend fun solveParallel(
//    height: Int,
//    width: Int,
//    map: Array<IntArray>,
//    degreemap: Array<IntArray>,
//    remaining: Int
//): String? {
////    val scope = CoroutineScope(Dispatchers.IO)
////    val scope = CoroutineScope(newFixedThreadPoolContext(threadNum, "solver"))
//    val threadNo = AtomicInteger()
//    val threadName = "solver"
//    val workQueue = LinkedBlockingQueue<java.lang.Runnable>()
//    val executor = ThreadPoolExecutor(
//        threadNum, 20,
//        0L, TimeUnit.MILLISECONDS,
//        workQueue
//    ) { runnable ->
//        val t = Thread(runnable, if (threadNum == 1) threadName else threadName + "-" + threadNo.incrementAndGet())
//        t.isDaemon = true
//        t
//    }
//
//    executor.submit {
//        runBlocking {
//            while (executor.isTerminating.not()) {
//                println("workQueue size ${workQueue.size}")
//                println("pool size ${executor.poolSize}")
//                println("corePoolSize ${executor.corePoolSize}")
//                println("activeCount ${executor.activeCount}")
//                delay(100)
//            }
//        }
//    }
//
//    val channel = Channel<String>(BUFFERED)
//
//    for (i in 0 until height) {
//        for (j in 0 until width) {
//            if (map[i][j] == 0) {
//                println("start $i $j")
//                executor.submit {
//                    val m = map.fullClone()
//                    val d = degreemap.fullClone()
//                    val path = singleSolve(height, width, m, remaining, i to j, i to j, "", d)
//                    if (path != "") {
//                        channel.trySend("($i $j) $path")
//                    }
//                }
////                scope.launch {
////                    val m = map.fullClone()
////                    val d = degreemap.fullClone()
////                    val path = singleSolve(height, width, m, remaining, i to j, i to j, "", d)
////                    if (path != "") {
////                        println(path)
////                        channel.send("($i $j) $path")
////                    }
////                }
//            }
//        }
//    }
//    while (channel.isClosedForSend.not()) {
//        val receive = channel.receive()
//        if (receive.isNotBlank()) {
////            scope.cancel()
//            return receive
//        }
//    }
////    scope.cancel()
//    return null
//}
//
//fun drawDegree(height: Int, width: Int, map: Array<IntArray>) {
//    for (i in 0 until height) {
//        for (j in 0 until width) {
//            print(map[i][j])
//        }
//        println()
//    }
//    println()
//
//}
//
//fun draw(height: Int, width: Int, map: Array<IntArray>, print: Boolean = true): String? {
//    return if (print) {
//        for (i in 0 until height) {
//            for (j in 0 until width) {
//                when (map[i][j]) {
//                    -1 -> {
//                        print("X")
//                    }
//
//                    0 -> {
//                        print(".")
//                    }
//
//                    else -> {
//                        print("*")
//                    }
//                }
//            }
//            println()
//        }
//        println()
//        null
//    } else {
//        val sb = StringBuilder()
//        for (i in 0 until height) {
//            for (j in 0 until width) {
//                when (map[i][j]) {
//                    -1 -> {
//                        sb.append("X")
//                    }
//
//                    0 -> {
//                        sb.append(".")
//                    }
//
//                    else -> {
//                        sb.append("*")
//                    }
//                }
//            }
//            sb.appendLine()
//        }
//        sb.appendLine()
//        sb.toString()
//    }
//}
//
//fun distance(a: Pair<Int, Int>, b: Pair<Int, Int>): Int {
//    return abs(a.first - b.first) + abs(a.second - b.second)
//}
//
//fun check(
//    height: Int,
//    width: Int,
//    map: Array<IntArray>,
//    remaining: Int,
//    cur: Pair<Int, Int>,
//    degreeMap: Array<IntArray>
//): Boolean {
//    var s = 0
//    var n = 1;
//    var start: Pair<Int, Int> = 0 to 0
//    val queue = ArrayBlockingQueue<Pair<Int, Int>>(width * height)
//    for (i in 0 until height) {
//        for (j in 0 until width) {
//            if (map[i][j] == 0) {
//                start = i to j
//                break
//            }
//        }
//        if (map[start.first][start.second] == 0) {
//            break
//        }
//    }
//    queue.add(start)
//
//    val m = map.fullClone()
//    m[start.first][start.second] = 1
//    val p = if (distance(start, cur) == 1) 1 else 0
//    if (degreeMap[start.first][start.second] + p == 1) {
//        s++
//    }
//
//    while (!queue.isEmpty()) {
//        val c = queue.poll()
//        for ((_, d) in directions) {
//            val next = c.first + d.first to c.second + d.second
//            if (valid(height, width, m, next)) {
//                m[next.first][next.second] = 1
//                queue.add(next)
//                n++
//                if (validaSize(height, width, next) && degreeMap[next.first][next.second] + p == 1) {
//                    s++
//                    if (s > 1) {
//                        return false
//                    }
//                }
//            }
//        }
//    }
//    return n == remaining
//}
//
//fun singleSolve(
//    height: Int,
//    width: Int,
//    map: Array<IntArray>,
//    remaining: Int,
//    start: Pair<Int, Int>,
//    cur: Pair<Int, Int>,
//    path: String,
//    degreeMap: Array<IntArray>
//): String {
//    var remaining = remaining
//    var cur = cur
//    var path = path
//    if (start == cur) {
//        map[cur.first][cur.second] = 1
//        degreeUpdate(height, width, map, cur, degreeMap)
//        remaining--
//    }
//    var c = cur
//    var r = remaining
//    for ((dir, d) in directions) {
//        var next = cur.first + d.first to cur.second + d.second
//        if (validaSize(height, width, next) && degreeMap[next.first][next.second] > 0) {
//            path += dir
//            println(Thread.currentThread().name + "$start $path")
//            while (validaSize(height, width, next) && degreeMap[next.first][next.second] > 0) {
//                map[next.first][next.second] = 1
//                degreeUpdate(height, width, map, next, degreeMap)
//                remaining--
//                next = next.first + d.first to next.second + d.second
//            }
//            cur = next.first - d.first to next.second - d.second
//
//            draw(height, width, map)
//            drawDegree(height, width, degreeMap)
//
//            if (remaining == 0) {
//                return path
//            }
//            if (check(height, width, map, remaining, cur, degreeMap)) {
//                val result = singleSolve(height, width, map, remaining, start, cur, path, degreeMap)
//                if (result != "") return result
//            }
//
//            next = c.first + d.first to c.second + d.second
//            while (validaSize(height, width, next) && cur != next) {
//                map[next.first][next.second] = 0
//                degreeUpdate(height, width, map, next, degreeMap)
//                remaining++
//                next = next.first + d.first to next.second + d.second
//            }
//            if (validaSize(height, width, next)) {
//                map[next.first][next.second] = 0
//                degreeUpdate(height, width, map, next, degreeMap)
//            }
////            remaining++
//
//            path = path.dropLast(1)
//            remaining = r
//        } else if (remaining == 1 && validaSize(height, width, next) && map[next.first][next.second] == 0) {
//            if (path.last() != dir) {
//                path += dir
//            }
//            return path
//        }
//    }
//
//    return ""
//}
//
//fun degree(height: Int, width: Int, map: Array<IntArray>, cur: Pair<Int, Int>): Int {
//    var n = 0
//    for ((_, d) in directions) {
//        val next = cur.first + d.first to cur.second + d.second
//        if (validaSize(height, width, next) && map[next.first][next.second] == 0) {
//            n++
//        }
//    }
//    return n
//}
//
//fun degreeUpdate(height: Int, width: Int, map: Array<IntArray>, cur: Pair<Int, Int>, degreeMap: Array<IntArray>) {
//    for ((_, d) in directions) {
//        val next = cur.first + d.first to cur.second + d.second
//        if (validaSize(height, width, next) && map[cur.first][cur.second] == 0 && map[next.first][next.second] == 0) {
//            degreeMap[next.first][next.second] = degree(height, width, map, next)
//        }
//    }
//    if (map[cur.first][cur.second] == 0) {
//        degreeMap[cur.first][cur.second] = degree(height, width, map, cur)
//    } else {
//        degreeMap[cur.first][cur.second] = 0
//    }
//}
//
//fun validaSize(height: Int, width: Int, cur: Pair<Int, Int>): Boolean {
//    return cur.first in 0..<height && cur.second in 0..<width
//}
//
//fun valid(height: Int, width: Int, m: Array<IntArray>, cur: Pair<Int, Int>): Boolean {
//    return validaSize(height, width, cur) && m[cur.first][cur.second] == 0
//}
//
//fun Array<IntArray>.fullClone(): Array<IntArray> {
//    val array = Array(this.size) { h ->
//        IntArray(this[h].size) { w ->
//            this[h][w]
//        }
//    }
//    return array
//}