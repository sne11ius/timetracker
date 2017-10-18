package wi.co.timetracker.model

enum class Severity {
    INFO,
    WARN,
    ERROR
}

data class ParseError(val severity: Severity, val index: Int, val message: String)

fun error(index: Int, message: String): ParseError {
    return ParseError(Severity.ERROR, index, message)
}

fun warn(index: Int, message: String): ParseError {
    return ParseError(Severity.WARN, index, message)
}

fun info(index: Int, message: String): ParseError {
    return ParseError(Severity.INFO, index, message)
}
