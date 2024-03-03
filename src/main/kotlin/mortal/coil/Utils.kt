package mortal.coil

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log = getLogger("Coil Solver")

fun Array<IntArray>.deepClone(): Array<IntArray> {
    val array = Array(this.size) { h ->
        IntArray(this[h].size) { w ->
            this[h][w]
        }
    }
    return array
}

fun Array<IntArray>.at(cur: Pair<Int, Int>) = this[cur.first][cur.second]

operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>) =
    this.first + other.first to this.second + other.second

operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>) =
    this.first - other.first to this.second - other.second

fun draw(solver: Solver, root: Solver.Root, node: Solver.Node, debug: Boolean = true) {
    if (debug.not()) return
    val sb = StringBuilder("\n")
    sb.appendLine("${root.start} [${node.path.length}]${node.path}")
    sb.appendLine()
    draw(solver.height, solver.width, node.map, debug, sb)
    drawDegree(solver.height, solver.width, node.degreeMap, debug, sb)
    log.info(sb.toString())
}

fun drawDegree(height: Int, width: Int, map: Array<IntArray>, debug: Boolean = true, sb: StringBuilder? = null) {
    if (debug.not()) return
    val print = sb == null
    val sb = sb?:StringBuilder("\n")
    for (i in 0 until height) {
        for (j in 0 until width) {
            sb.append(map[i][j])
        }
        sb.appendLine()
    }
    sb.appendLine()
    if (print) log.info(sb.toString())
}

fun draw(height: Int, width: Int, map: Array<IntArray>, debug: Boolean = true, sb: StringBuilder? = null) {
    if (debug.not()) return
    val print = sb == null
    val sb = sb?:StringBuilder("\n")
    for (i in 0 until height) {
        for (j in 0 until width) {
            when (map[i][j]) {
                -1 -> {
                    sb.append("X")
                }

                0 -> {
                    sb.append(".")
                }

                else -> {
                    sb.append("*")
                }
            }
        }
        sb.appendLine()
    }
    sb.appendLine()
    if (print) log.info(sb.toString())
}

fun getLogger(name: String = Thread.currentThread().stackTrace[2].className): Logger {
    return LoggerFactory.getILoggerFactory().getLogger(name)
}