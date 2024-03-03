package mortal.coil

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.writeText
import kotlin.time.measureTimedValue

//https://www.hacker.org/coil/?x=3&y=1&path=LDLURD

const val URL = "https://www.hacker.org/coil/"
const val DEBUG: Boolean = false
const val PARALLEL_NUM: Int = 5
const val LIMIT_TIMES: Int = 1


val client = HttpClient(OkHttp) {
    engine {
        config {
            followRedirects(true)
        }
    }
}
val scope = CoroutineScope(Dispatchers.IO)
var cookie = ""
var script = ""
var level = 0
val levelRegex = "Level: \\d+".toRegex()
val cookiePath = Path.of("./cookie.txt")
val scriptPath = Path.of("./script.txt")
suspend fun main() {
    initCookie()
    var currLog = getCurrLog()
    var count = 0
    while (count < LIMIT_TIMES) {
        val (result, duration) = measureTimedValue {
            Solver(currLog.height, currLog.width, currLog.boardStr, scope = scope, debug = DEBUG).solveParallel()
        }

        log.info("time $duration")

        if (result == null) throw IllegalStateException("No solution")

        val root = result.first
        val node = result.second

        currLog.x = root.start.second
        currLog.y = root.start.first
        currLog.path = node.path


        val res = client.get(URL) {
            parameter("x", root.start.second)
            parameter("y", root.start.first)
            parameter("path", node.path)
            header(HttpHeaders.Cookie, cookie)
        }

        val text = res.bodyAsText()

        if (text.contains("your solution sucked")) {
            client.close()
            throw IllegalStateException("your solution sucked")
        }
        log.info(currLog.toString())

        setCoolie(res)
        getScript(text)

        currLog = parseScript()
        count++
    }
    afterSolve()

    client.close()
    scope.cancel()
}

fun initCookie() {
    if (cookiePath.exists().not()) {
        cookiePath.createFile()
    } else {
        Files.readString(cookiePath, StandardCharsets.UTF_8).also {
            log.info("Load cookie: $it")
            cookie = it
        }
    }
}

fun afterSolve() {
    saveCookie()
    saveScript()

    client.close()
    scope.cancel()
}

fun saveCookie() {
    log.info("Save cookie: $cookie")
    cookiePath.writeText(cookie, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
}

fun saveScript() {
    log.info("Save script: $script")
    scriptPath.writeText(script, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
}

suspend fun getCurrLog(): SolveLog {
    if (scriptPath.exists().not()) {
        scriptPath.createFile()
        return getCurrLogByUrl()
    } else {
        script = withContext(Dispatchers.IO) {
            Files.readString(scriptPath)
        }
        log.info("Load script: $script")
    }
    return if (script.isBlank()) {
        getCurrLogByUrl()
    } else {
        parseScript()
    }
}

fun setCoolie(res: HttpResponse) {
    res.setCookie().takeIf { it.isNotEmpty() }?.let { c ->
        cookie = c.joinToString(";") { renderCookieHeader(it) }.also {
            log.info("Set cookie: $it")
        }
    }
}

suspend fun getCurrLogByUrl(): SolveLog {
    val res = client.get(URL) {
        header("Cookie", cookie)
    }
    setCoolie(res)
    val body = res.bodyAsText()
    getScript(body)

    // 'var curLevel = 4; var width = 7; var height = 6; var boardStr = ".................XX..X..XX.....X......X...";'
    return parseScript()
}

fun getScript(body: String) {
    val document = Jsoup.parse(body)
    script = document.selectXpath("//td[@id=\"pgfirst\"]/script").first()!!.data()
    level = levelRegex.find(body)?.value?.substringAfter("Level: ")?.toInt()?: 0
    script = script.replace("curLevel = 4", "curLevel = $level")
    log.info("Get script: $script")
}

fun parseScript(): SolveLog {
    val split = script.split(";").map { it.substringAfterLast('=') }

    return SolveLog(level, split[1].trim().toInt(), split[2].trim().toInt(), split[3].trim().removeSurrounding("\""))
}

@Serializable
data class SolveLog(
    val level: Int,
    val width: Int,
    val height: Int,
    val boardStr: String,
    var x: Int? = null,
    var y: Int? = null,
    var path: String? = null
)