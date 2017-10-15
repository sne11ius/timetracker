package wi.co.timetracker.model

enum class Severity {
    INFO,
    WARN,
    ERROR
}

data class ParseError(val severity: Severity, val line: Int, val message: String)

fun error(line: Int, message: String): ParseError {
    return ParseError(Severity.ERROR, line, message)
}

fun warn(line: Int, message: String): ParseError {
    return ParseError(Severity.WARN, line, message)
}

fun info(line: Int, message: String): ParseError {
    return ParseError(Severity.INFO, line, message)
}
