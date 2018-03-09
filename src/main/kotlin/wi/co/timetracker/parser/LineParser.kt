package wi.co.timetracker.parser

import mu.KotlinLogging
import org.springframework.stereotype.Component
import wi.co.timetracker.model.EntryModel
import wi.co.timetracker.model.ParseError
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Collections.emptyList

const val MAX_TEXT_LENGTH = 200
const val MAX_COMMENT_LENGTH = 200
const val MAX_SUM_HOURS = 2
const val MAX_SUM_MINUTE_PART = 3

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

    data class SingleLineParseResult(val errors: List<ParseError>, val entry: EntryModel?)

    private sealed class ParseResult {
        data class ParserState(
                val state: State = State.HOURS_START,
                val hoursStart: String = "",
                val minutesStart: String = "",
                val skipToEnd: String = "",
                val hoursEnd: String = "",
                val minutesEnd: String = "",
                val skipToText: String = "",
                val text: String = "",
                val comment: String = "",
                val skipToSumFromComment: String = "",
                val sumHours: String = "",
                val sumMinutePart: String = ""
        ): ParseResult()
        data class ParseError(
                val charIndex: Int,
                val msg: String
        ): ParseResult()
    }

    private fun cannotError(state: State, charIndex: Int, char: Char)
            = ParseResult.ParseError(charIndex, "This should not happen at state $state: char $char at index is $charIndex")

    private fun charError(charIndex: Int, expectedChar: String, actualChar: Char)
            = ParseResult.ParseError(charIndex, "Unexpected character '$actualChar'. Expected $expectedChar.")

    private val stateHandlers: Map<State, (Char, Int, ParseResult.ParserState) -> ParseResult> = mapOf(
            State.HOURS_START to { char, charIndex, state ->
                val hoursStart = state.hoursStart + char
                if (state.hoursStart.isEmpty()) {
                    if (char in '0'..'2') {
                        state.copy(hoursStart = hoursStart)
                    } else
                        charError(charIndex, "0..2", char)
                } else if (1 == state.hoursStart.length) {
                    if (state.hoursStart.startsWith("0") || state.hoursStart.startsWith("1")) {
                        if (char in '0'..'9') {
                            state.copy(hoursStart = hoursStart, state = State.SKIP_TO_MINUTES_START)
                        } else
                            charError(charIndex, "0..9", char)
                    } else if (state.hoursStart.startsWith("2")) {
                        if (char in '0'..'3') {
                            state.copy(hoursStart = hoursStart, state = State.SKIP_TO_MINUTES_START)
                        } else
                            charError(charIndex, "0..3", char)
                    } else {
                        cannotError(state.state, charIndex, char)
                    }
                } else
                    charError(charIndex, "0..9", char)
            },
            State.SKIP_TO_MINUTES_START to { char, charIndex, state ->
                if (char == ':') {
                    state.copy(state = State.MINUTES_START)
                } else {
                    charError(charIndex, ":", char)
                }
            },
            State.MINUTES_START to { char, charIndex, state ->
                if (char in '0'..'9') {
                    val minutesStart = state.minutesStart + char
                    if (minutesStart.length == 2) {
                        state.copy(minutesStart = minutesStart, state = State.SKIP_TO_HOURS_END)
                    } else {
                        state.copy(minutesStart = minutesStart)
                    }
                } else
                    charError(charIndex, "0..9", char)
            },
            State.SKIP_TO_HOURS_END to { char, charIndex, state ->
                if (state.skipToEnd == "") {
                    if (char == ' ') {
                        state.copy(skipToEnd = " ")
                    } else {
                        charError(charIndex, "' '", char)
                    }
                } else if (state.skipToEnd == " ") {
                    if (char == '-') {
                        state.copy(skipToEnd = " -")
                    } else {
                        charError(charIndex, "-", char)
                    }
                } else if (state.skipToEnd == " -") {
                    if (char == ' ') {
                        state.copy(skipToEnd = " - ", state = State.HOURS_END)
                    } else {
                        charError(charIndex, "' '", char)
                    }
                } else {
                    cannotError(state.state, charIndex, char)
                }
            },
            State.HOURS_END to { char, charIndex, state ->
                val hoursEnd = state.hoursEnd + char
                if (state.hoursEnd.isEmpty()) {
                    if (char in '0'..'2') {
                        state.copy(hoursEnd = hoursEnd)
                    } else
                        charError(charIndex, "0..2", char)
                } else if (1 == state.hoursEnd.length) {
                    if (hoursEnd.startsWith("0") || hoursEnd.startsWith("1")) {
                        if (char in '0'..'9') {
                            state.copy(hoursEnd = hoursEnd, state = State.SKIP_TO_MINUTES_END)
                        } else
                            charError(charIndex, "0..9", char)
                    } else if (hoursEnd.startsWith("2")) {
                        if (char in '0'..'3') {
                            state.copy(hoursEnd = hoursEnd, state = State.SKIP_TO_MINUTES_END)
                        } else
                            charError(charIndex, "0..3", char)
                    } else {
                        cannotError(state.state, charIndex, char)
                    }
                } else
                    charError(charIndex, "0..9", char)
            },
            State.SKIP_TO_MINUTES_END to { char, charIndex, state ->
                if (char == ':') {
                    state.copy(state = State.MINUTES_END)
                } else {
                    charError(charIndex, ":", char)
                }
            },
            State.MINUTES_END to { char, charIndex, state ->
                val minutesEnd = state.minutesEnd + char
                if (char in '0'..'9') {
                    if (minutesEnd.length == 2) {
                        state.copy(minutesEnd = minutesEnd, state = State.SKIP_TO_TEXT)
                    } else {
                        state.copy(minutesEnd = minutesEnd)
                    }
                } else
                    charError(charIndex, "0..9", char)
            },
            State.SKIP_TO_TEXT to { char, charIndex, state ->
                if (state.skipToText == "") {
                    if (char == ':')
                        state.copy(skipToText = ":")
                    else
                        charError(charIndex, "' '", char)
                } else if (state.skipToText == ":") {
                    if (char == ' ') {
                        state.copy(skipToText = ": ", state = State.TEXT)
                    } else {
                    charError(charIndex, "'-'", char)
                    }
                } else {
                    cannotError(state.state, charIndex, char)
                }
            },
            State.TEXT to { char, charIndex, state -> when {
                    charIndex > MAX_TEXT_LENGTH -> ParseResult.ParseError(charIndex, "Max text length of $MAX_TEXT_LENGTH exceeded.")
                    char == ',' -> charError(charIndex, "not ','", char)
                    char == '(' -> state.copy(state = State.COMMENT)
                    char == '=' -> state.copy(state = State.SKIP_TO_SUM_FROM_TEXT)
                    else -> state.copy(text = state.text + char)
                }
            },
            State.COMMENT to { char, charIndex, state -> when {
                    charIndex > MAX_COMMENT_LENGTH -> ParseResult.ParseError(charIndex, "Max comment length of $MAX_COMMENT_LENGTH exceeded.")
                    char == ')' -> state.copy(state = State.SKIP_TO_SUM_FROM_COMMENT)
                    else -> state.copy(comment = state.comment + char)
                }
            },
            State.SKIP_TO_SUM_FROM_TEXT to { char, charIndex, state ->
                if (char == ' ') {
                    state.copy(state = State.SUM_HOURS)
                } else {
                    charError(charIndex, "' '", char)
                }
            },
            State.SKIP_TO_SUM_FROM_COMMENT to { char, charIndex, state ->
                if (state.skipToSumFromComment == "") {
                    if (char == ' ') {
                        state.copy(skipToSumFromComment = " ")
                    } else
                        charError(charIndex, "' '", char)
                } else if (state.skipToSumFromComment == " ") {
                    if (char == '=') {
                        state.copy(skipToSumFromComment = " ")
                    }
                    charError(charIndex, "=", char)
                } else if (state.skipToSumFromComment == " =") {
                    if (char == ' ') {
                        state.copy(state = State.SUM_HOURS)
                    } else
                        charError(charIndex, "=", char)
                } else {
                    cannotError(state.state, charIndex, char)
                }
            },
            State.SUM_HOURS to { char, charIndex, state ->
                if (state.sumHours.length > MAX_SUM_HOURS)
                    ParseResult.ParseError(charIndex, "Max hour part length exceeded.")
                else when (char) {
                    in '0'..'9' -> state.copy(sumHours = state.sumHours + char)
                    ',' -> state.copy(state = State.SUM_MINUTE_PART)
                    else ->charError(charIndex, "',' or '0'..'9'", char)
                }
            },
            State.SUM_MINUTE_PART to { char, charIndex, state ->
                when {
                    state.sumMinutePart.length > MAX_SUM_MINUTE_PART -> ParseResult.ParseError(charIndex, "Max minute part length exceeded.")
                    char in '0'..'9' -> state.copy(sumMinutePart = state.sumMinutePart + char)
                    else ->charError(charIndex, "0..9", char)
                }
            }
    )

    fun parseLine(baseDate: LocalDate, line: String): SingleLineParseResult {
        var state: ParseResult = ParseResult.ParserState()
        log.debug { "Parse line $line" }
        for (i in 0 until line.length) {
            val char = line[i]
            state = when (state) {
                is ParseResult.ParserState -> stateHandlers[state.state]!!.invoke(char, i, state)
                is ParseResult.ParseError -> {
                    return err(state.charIndex, state.msg)
                }
            }
        }

        when (state) {
            is ParseResult.ParserState -> {
                when (state.state) {
                    State.TEXT, State.SKIP_TO_SUM_FROM_TEXT, State.SKIP_TO_SUM_FROM_COMMENT, State.SUM_HOURS, State.SUM_MINUTE_PART -> {
                        return mkEntry(
                                baseDate,
                                state.hoursStart,
                                state.minutesStart,
                                state.hoursEnd,
                                state.minutesEnd,
                                state.text,
                                state.comment
                        )
                    }
                    else -> {
                        return err(line.length, "Incomplete entry, please finish $state.")
                    }
                }
            }
            else -> return err(line.length, "$state.")
        }
    }

    private fun err(index: Int, msg: String): SingleLineParseResult {
        return SingleLineParseResult(listOf(wi.co.timetracker.model.error(index, msg)), null)
    }

    private fun mkEntry(baseDate: LocalDate, hoursStart: String, minutesStart: String, hoursEnd: String, minutesEnd: String, text: String, comment: String): SingleLineParseResult {
        val baseTime = LocalDateTime.of(baseDate, LocalTime.now()).withHour(0).withMinute(0).withSecond(0).withNano(0)
        val start = try {
            baseTime.withHour(hoursStart.toInt()).withMinute(minutesStart.toInt())
        } catch (e: Exception) {
            return err(-1, "Could not parse start time $hoursStart:$minutesStart: ${e.message}")
        }
        val end = try {
            baseTime.withHour(hoursEnd.toInt()).withMinute(minutesEnd.toInt())
        } catch (e: Exception) {
            return err(-1, "Could not parse end time $hoursEnd:$minutesEnd: ${e.message}")
        }
        if (text.isBlank()) {
            return err(-1, "Empty text")
        }
        val model = EntryModel(start, end, text.trim(), comment.trim())
        return SingleLineParseResult(emptyList(), model)
    }

}
