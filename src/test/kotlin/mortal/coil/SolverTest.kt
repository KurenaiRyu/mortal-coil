package mortal.coil

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import kotlin.test.fail
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

class SolverTest {

    companion object {
        const val DEBUG: Boolean = false;
        const val PARALLEL_NUM: Int = 20;
    }

    @Test
    fun level87() {
        val level = 87
        val height = 32;
        val width = 32;
        val mapStr =
            ".....XXXXXX.....X..XXX......XXX....X.XXXXXX..XX....XX....X..XXX.XX.X..XXXXXX.....X....X..X....X.XX.XX.XXX...XXX....X..X....X....XX....X...X...X.X....XX...XX.X.X....X......XX.....X.....X..X...X.XX......X.....X....X......XX.......XX...XX.....XX....XXXX....X...X..X...X..X.X....X..XX...X....XXXX...X.X.X..X..X...X...X...X.X.......X.....X.........X.......X...XX.....X....XXXX.X.......XX........XXX.....XXXX..X...X....X....X........XX...XX........XX........XXXXXX.XXX..X..X...X..X...X....XXX.....X...XX....X...XX.X........X.......XXXX...XX..XX..X..X..XX......X.......XXXXX....XX...X.....XXX...XXX...XX........X.X...X...X.........X.XX..XXXXX...X..XX.............X...X...XXXX........XXXX...X......X.......X....XX.....X..X...X......XX.X..X....X..XX....X......X.XX....X.X...X...XXX...X..X....X..XX.......X.........X...XXXX.....XX...XX.....XX..........XX..XXXX.....XX...X.......X...X..X.XX.....X.....X.....XXX...X..X...XX.X.X...XXX....XXXX........X......X....XX...XX....X..X...XXX.XXX..XX.......X...XX.XX...XXXXX.............X...X...."
        solve(height, width, mapStr)
    }

    @Test
    fun level9() {
        val level = 9
        val height = 6;
        val width = 6;
        val mapStr =
            "...X...X.X............X..X..X......."
        solve(height, width, mapStr)
    }

    @Test
    fun level4() {
        val level = 4
        val height = 3
        val width = 3
        val mapStr =
            "X.......X"
        solve(height, width, mapStr)
    }

    @Test
    fun level170() {
        val level = 170
        val height = 59;
        val width = 61;
        val mapStr =
            "...XXXX........X........XXX...........X.....XXXXXXXXX..XXXXXX...X......X....X........X...XXX.....X...XXX.XXXXXXX....XX...XX..X.X......X..X..XXXX..X.XXXXX.XXX.XX....X.X.......XX....X.X...X.X...XXX...XXXXXX...X.......X...X..X....X.......XXXX..X.X.X.....X...X.X.XX.....X..XXXXX..X.XXX.XX.X.....XX...XXX..XX.X....X....X...X.....X..XX.X......X..X..XX.....X......XXX.....X..X.XXX...XXX..X.X.XXXXX.X.XXXX..X.X...X..XXXX...X..XXX...X....X.XX..X......X...XX....X....XX.....X....XXXXXX.X....X.XX.......XX...X.........XX.....X...........X...XXXX.....X....X..X..X.....X........X..X....X....X......X..XX.X....XX....XX...XX....X...X..X...XXX.X..XX....X....XXX..X..X.....XX..XXX........X......X....X..XX.X...X.......X..X...XX....X..XX...X.....X.....X......XX....XX.XXX.....X...XX...X.........XX..X...X...X.X..XX...X......XX...X....X......X........XXXX..XX...X..XXX........XX.....XX....XXX.XXX..X...............X...X..X...XXX.....XXX...X.......X....X...X.X....X.X......X..X...X...XX...X.X.........XXX...X.XXX....X...X.X....X..X...X.XXXXXXXX...XX.X.......XX....X.............X..X.X.X...X.X...X..XX...X..X..X....XX......XX....X.X......X....X....XX...X....X....X.X...X....X.......XXXX..X...X..X......X....X...X..XXXX..X.......X....X.....X......XXXXX...X........X..XXXXX......XXX.X.XX.....XXXX...XX.X..XXX.......X..X.......X..X..XX......XXX...XX...X..X...X.XX......X....XX..X.X..XX................XXX....XX.X....X...X..X..X.....XX.......XX.........XX...X....XX..XX....X..XX....XX....X....X..........XX...X.......X.X.....X.X....XXX.....X..X.....X..XXX.....X.XX..X.X.....X.....X...X.X.......XX.....X..X.XXXXX..XXXX....X.X...X....XX.XXXX....X...........XXX......X..XXX.....X...X....X......XXXX..XX..X....XX.....X......X.X....XX.......X.....X.....X...X..XX.X..X....XX......X.XXX..X...X......X.......X....X.....X.........XX.X...X.X........X...X.......X....XX...X.X.....X....XX..XX.XXX...X...X.X...X....X...X........X.XXXX.......X...XX..X...X...XX...X......X.X.............X...X.XXX.....X...X....X...X...X.X..X.X.X.X..X.XXXXX.....X...X...X...X.......X....X..XXXXXX.X...XX...X.X..X...XX....X.....X...X......XXX.....X.XX..XX....XX......XX....XX.....XX...XX....X....X.....XXX.......X....X.....XX...X...X..X.X.XXX....XX....XX.X........XXXXX.....XX..X...X....XXX.X..X.X.X.....X.....X...X.X..X...X...XX....X.X.....X...X..X...XX.X...........X.....X...XX...X.XX.....X......X.......X....X..X......XXX......XX.XX..XXXXX...XX.X.....X.......X......X.....X..X........XX.....XXX.....X....X....X.....XX.X.X...X........X....XXXX.X.XX.XX...XX...X...XX..XXX...X.X.XX.......X.X......X.X..XXXX......XX.X..........XXX...XX..X.......X....X....XXX......XX...XX.X....XXXXX.XX...XX..X....XXX...X..XXXX....X......X...X..X.XX.....XX.....XX...XX....X...X..X...XXXXXX...XX.........X......X.......X..X.....XX......X.X.........X...X....XXXX........X......XXX....X..X..XX...XX....XX.....X.X.X.......XX..X...X....X.XX......X..X...XXX...X...X...XX.......X..XXX..XX......X.X....X..XX.X.XX.XX......X...X..X.....XX.X..X..X...XXX........X.X....XXX.X.......X.XXXX..X...X.X.X....X..XX.....X.....XXX..X.....X....XX..X..X..XXXXX.X.....X....XXX..XX.XX..X.X...XXX.....X..X.....X..XX.X.XXX......XX.....X......X....XXX.X..XXXXXX.....X....X....X..X...X.X....XX...X......X...X......XX....XXXXXX..X.X....XX...X..X...X.....X.XX...XX..X..XX...XXXXXX.....XXX...X...X.......X.....X....X.X....X..X.XX..X..XX..X..........X...XX....XXXXX.X...X.X.X.........XX....X....X....X...X..........X..X...X......X.....X.X...XXX.....X.X.......X.....XXX...X....X....X...X...XX.XX....X........XXXX.X...X.X....X...XX.......X.....XX..X...XX..............XXXXXX...X...X..X...X.......XX...XXX......."
        solve(height, width, mapStr)
    }

    @Test
    fun level25() {
        val level = 25
        val height = 11;
        val width = 12;
        val mapStr = ".........X........XX....XX.............XXX.......XXX......X......X......XXXX...XXX.X.......X...X..X....X......XXX.....X.........X..."
        solve(height, width, mapStr)
    }

    @Test
    fun level50() {
        val level = 50
        val height = 19;
        val width = 21;
        val mapStr = "X....X....X...XX...XX..XX...XX...X....X..X.X.......X....X.....X...X.......X...XX.X..X..X..X......X....X..X.XXX...........X....X......X....X.XX........X...XX.....XX.X....X..XX.........X.X....XX......X...X...XXX...X..XXX...X..X..XX....X.........X....X..X........XXX....X.X....X...XX.XX.....X..XX..X...XX....XX...X.X.....X.....X....X.X...X.......X...X..X.X..XX............XX..X..............X....XX...."
        solve(height, width, mapStr)
    }

    private fun solve(height: Int, width: Int, mapStr: String) {
        val scope = CoroutineScope(Dispatchers.IO)
        runBlocking {
            val solver: Solver
            val (result, duration) = measureTimedValue {
                solver = Solver(height, width, mapStr, debug = DEBUG, parent = scope)
                solver.solve().also {
                    Assert.assertNotNull(it)
                }
            }
            log.info("Parallel: ${solver.parallel}")
            log.info("Result: ${solver.startCount}/${solver.validPoints}(${solver.remaining}) $result")
            log.info("time: $duration")
        }
    }

    @Test
    fun testArray() {
        val origin = Array(10) {
            IntArray(10) {
                0
            }
        }
        val copy = origin.deepClone()
        copy[0][0] = 1
        println(origin[0][0] == copy[0][0])
        println(origin.contentToString())
        println(copy.contentToString())
    }

    private fun Array<IntArray>.contentToString(): String {
        return this.joinToString(",") { it.contentToString() }
    }
}