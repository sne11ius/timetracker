package wi.co.timetracker.service

import mu.KotlinLogging
import org.springframework.stereotype.Component
import wi.co.timetracker.model.EntryModel
import wi.co.timetracker.model.ParseError
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Collections.emptyList


data class SingleLineParseResult(val errors: List<ParseError>, val entry: EntryModel?)

const val MAX_TEXT_LENGTH = 200
const val MAX_COMMENT_LENGTH = 200
const val MAX_SUM_HOURS = 2
const val MAX_SUM_MINUTE_PART = 2

@Component
class LineParser {

    private val log = KotlinLogging.logger {}

    private enum class State {
        HOURS_START,
        SKIP_TO_MINUTES_START,
        MINUTES_START,
        SKIP_TO_HOURS_END,
        HOURS_END,
        SKIP_TO_MINUTES_END,
        MINUTES_END,
        SKIP_TO_TEXT,
        TEXT,
        COMMENT,
        SKIP_TO_SUM_FROM_TEXT,
        SKIP_TO_SUM_FROM_COMMENT,
        SUM_HOURS,
        SUM_MINUTE_PART
    }

    fun parseLine(baseDate: LocalDateTime, line: String): SingleLineParseResult {
        var state = State.HOURS_START
        var hoursStart = ""
        var minutesStart = ""
        var skipToEnd = ""
        var hoursEnd = ""
        var minutesEnd = ""
        var skipToText = ""
        var text = ""
        var comment = ""
        var skipToSumFromComment = ""
        var sumHours = ""
        var sumMinutePart = ""

        log.debug { "Parse line $line" }

        for (i in 0 until line.length) {
            val char = line[i]
            log.debug { "Parse '$char' at $i" }
            when (state) {
                State.HOURS_START -> if (0 == hoursStart.length) {
                    if (char in '0'..'2') {
                        hoursStart += char
                        if (hoursStart.length == 2) {
                            state = State.SKIP_TO_MINUTES_START
                        }
                    } else
                        return err(i, "Unexpected character '$char'. Expected 0..2.")
                } else if (1 == hoursStart.length) {
                    if (hoursStart.startsWith("0") || hoursStart.startsWith("1")) {
                        if (char in '0'..'9') {
                            hoursStart += char
                            state = State.SKIP_TO_MINUTES_START
                        } else
                            return err(i, "Unexpected character '$char'. Expected 0..9.")
                    } else if (hoursStart.startsWith("2")) {
                        if (char in '0'..'3') {
                            hoursStart += char
                            state = State.SKIP_TO_MINUTES_START
                        } else
                            return err(i, "Unexpected character '$char'. Expected 0..3.")
                    }
                } else
                    return err(i, "Unexpected character '$char'. Expected 0..9.")
                State.SKIP_TO_MINUTES_START -> if (char == ':') {
                    state = State.MINUTES_START
                } else {
                    return err(i, "Unexpected character '$char'. Expected :.")
                }
                State.MINUTES_START -> if (char in '0'..'9') {
                    minutesStart += char
                    if (minutesStart.length == 2) {
                        state = State.SKIP_TO_HOURS_END
                    }
                } else
                    return err(i, "Unexpected character '$char'. Expected 0..9.")
                State.SKIP_TO_HOURS_END -> if (skipToEnd == "") {
                    if (char == ' ') {
                        skipToEnd = " "
                    } else {
                        return err(i, "Unexpected character '$char'. Expected ' '.")
                    }
                } else if (skipToEnd == " ") {
                    if (char == '-') {
                        skipToEnd = " -"
                    } else {
                        return err(i, "Unexpected character '$char'. Expected '-'.")
                    }
                } else if (skipToEnd == " -") {
                    if (char == ' ') {
                        skipToEnd = " - "
                        state = State.HOURS_END
                    } else {
                        return err(i, "Unexpected character '$char'. Expected ' '.")
                    }
                }
                State.HOURS_END -> if (0 == hoursEnd.length) {
                    if (char in '0'..'2') {
                        hoursEnd += char
                        if (hoursEnd.length == 2) {
                            state = State.SKIP_TO_MINUTES_END
                        }
                    } else
                        return err(i, "Unexpected character '$char'. Expected 0..2.")
                } else if (1 == hoursEnd.length) {
                    if (hoursEnd.startsWith("0") || hoursEnd.startsWith("1")) {
                        if (char in '0'..'9') {
                            hoursEnd += char
                            state = State.SKIP_TO_MINUTES_END
                        } else
                            return err(i, "Unexpected character '$char'. Expected 0..9.")
                    } else if (hoursEnd.startsWith("2")) {
                        if (char in '0'..'3') {
                            hoursEnd += char
                            state = State.SKIP_TO_MINUTES_END
                        } else
                            return err(i, "Unexpected character '$char'. Expected 0..3.")
                    }
                } else
                    return err(i, "Unexpected character '$char'. Expected 0..9.")
                State.SKIP_TO_MINUTES_END -> {
                    if (char == ':') {
                        state = State.MINUTES_END
                    } else {
                        return err(i, "Unexpected character '$char'. Expected :.")
                    }
                }
                State.MINUTES_END -> if (char in '0'..'9') {
                    minutesEnd += char
                    if (minutesEnd.length == 2) {
                        state = State.SKIP_TO_TEXT
                    }
                } else
                    return err(i, "Unexpected character '$char'. Expected 0..9.")
                State.SKIP_TO_TEXT -> if (skipToText == "") {
                    skipToText = if (char == ':')
                        ":"
                    else
                        return err(i, "Unexpected character '$char'. Expected ' '.")
                } else if (skipToText == ":") {
                    if (char == ' ') {
                        skipToText = ": "
                        state = State.TEXT
                    } else {
                        return err(i, "Unexpected character '$char'. Expected '-'.")
                    }
                }
                State.TEXT -> when {
                    i > MAX_TEXT_LENGTH -> return err(i, "Max text length of $MAX_TEXT_LENGTH exceeded.")
                    char == ',' ->
                        return err(i, "Unexpected character '$char'. Expected ','.")
                    char == '(' -> state = State.COMMENT
                    char == '=' -> state = State.SKIP_TO_SUM_FROM_TEXT
                    else -> text += char
                }
                State.COMMENT -> when {
                    i > MAX_COMMENT_LENGTH ->
                        return err(i, "Max comment length of $MAX_COMMENT_LENGTH exceeded.")
                    char == ')' -> state = State.SKIP_TO_SUM_FROM_COMMENT
                    else -> comment += char
                }
                State.SKIP_TO_SUM_FROM_TEXT -> if (char == ' ') {
                    state = State.SUM_HOURS
                } else {
                    return err(i, "Unexpected character '$char'. Expected ' '.")
                }
                State.SKIP_TO_SUM_FROM_COMMENT -> {
                    if (skipToSumFromComment == "") {
                        if (char == ' ') {
                            skipToSumFromComment = " "
                        } else
                            return err(i, "Unexpected character '$char'. Expected ' '.")
                    } else if (skipToSumFromComment == " ") {
                        if (char == '=') {
                            skipToSumFromComment = " ="
                        } else
                            return err(i, "Unexpected character '$char'. Expected '='.")
                    } else if (skipToSumFromComment == " =") {
                        if (char == ' ') {
                            state = State.SUM_HOURS
                        } else
                            return err(i, "Unexpected character '$char'. Expected '='.")
                    }
                }

                State.SUM_HOURS -> if (sumHours.length > MAX_SUM_HOURS)
                    return err(i, "Max hour part length exceeded.")
                else when (char) {
                    in '0'..'9' -> sumHours += char
                    ',' -> state = State.SUM_MINUTE_PART
                    else ->
                        return err(i, "Unexpected character '$char'. Expected ',' or '0'..'9'.")
                }
                State.SUM_MINUTE_PART -> sumMinutePart += when {
                    sumMinutePart.length > MAX_SUM_MINUTE_PART -> return err(i, "Max minute part length exceeded.")
                    char in '0'..'9' -> char
                    else ->
                        return err(i, "Unexpected character '$char'. Expected '0'..'9'.")
                }
            }
        }

        when (state) {
            State.TEXT, State.SKIP_TO_SUM_FROM_TEXT, State.SKIP_TO_SUM_FROM_COMMENT, State.SUM_HOURS, State.SUM_MINUTE_PART -> {
                return mkEntry(
                        baseDate,
                        hoursStart,
                        minutesStart,
                        hoursEnd,
                        minutesEnd,
                        text,
                        comment,
                        sumHours,
                        sumMinutePart
                )
            }
            else -> {
                return err(line.length, "Incomplete entry, please finish $state.")
            }
        }
    }

    private fun err(index: Int, msg: String): SingleLineParseResult {
        return SingleLineParseResult(listOf(wi.co.timetracker.model.error(index, msg)), null)
    }

    private fun mkEntry(baseDate: LocalDateTime, hoursStart: String, minutesStart: String, hoursEnd: String, minutesEnd: String, text: String, comment: String, sumHours: String, sumMinutePart: String): SingleLineParseResult {
        val start = try {
            baseDate.withHour(hoursStart.toInt()).withMinute(minutesStart.toInt())
        } catch (e: Exception) {
            return err(-1, "Could not parse start time $hoursStart:$minutesStart: ${e.message}")
        }
        val end = try {
            baseDate.withHour(hoursEnd.toInt()).withMinute(minutesEnd.toInt())
        } catch (e: Exception) {
            return err(-1, "Could not parse end time $hoursEnd:$minutesEnd: ${e.message}")
        }
        if (text.isBlank()) {
            return err(-1, "Empty text")
        }
        val model = EntryModel(start, end, text.trim(), comment.trim())
        val minutes = "0.${if (sumMinutePart.isEmpty()) "0" else sumMinutePart}".toFloat() * 60
        if (sumHours.isNotBlank() || sumMinutePart.isNotBlank()) {
            val notedDuration = try {
                Duration
                        .ofHours(if (sumHours.isBlank()) 0 else sumHours.toLong())
                        .plusMinutes(minutes.toLong())
            } catch (e: Exception) {
                return err(-1, "Could not parse sum $sumHours,$sumMinutePart: ${e.message}")
            }
            if (notedDuration != model.duration()) {
                val diff = model.duration().minus(notedDuration).abs()
                val diffString = LocalTime.MIDNIGHT.plus(diff).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                return err(-1, "Duration mismatch of $diffString.")
            }
        }
        return SingleLineParseResult(emptyList(), model)
    }

}
