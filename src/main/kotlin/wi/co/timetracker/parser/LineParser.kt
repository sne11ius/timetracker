package wi.co.timetracker.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.tryParseToEnd
import com.github.h0tk3y.betterParse.parser.ErrorResult
import com.github.h0tk3y.betterParse.parser.Parsed
import org.springframework.stereotype.Component
import wi.co.timetracker.model.entry.EntryModel
import wi.co.timetracker.model.parser.ParseError
import wi.co.timetracker.model.parser.Severity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit.MINUTES

@Component
class LineParser : Grammar<EntryModel>() {

  data class SingleLineParseResult(val errors: List<ParseError>, val entry: EntryModel? = null)

  private val hours by token("[01][0-9]|2[0-3]")
  private val minutes by token("[0-5][0-9]")
  private val sep by token(":")
  private val fromToSep by token(" - ")
  private val space by token(" ")
  private val lparen by token("\\(", ignore = true)
  private val rparen by token("\\)", ignore = true)
  private val text by token("[^()]+")

  private val hoursP = hours use { text.toInt() }
  private val minutesP by (hours or minutes) use { text.toInt() }
  private val textP = text use { text.trim() }

  val time = hoursP and sep and minutesP
  private val timeP = time map { (h, _, m) -> LocalDateTime.now().truncatedTo(MINUTES).withHour(h).withMinute(m) }
  private val fromTo = timeP and fromToSep and timeP
  private val fromToP = fromTo map { (begin, _, end) -> Pair(begin, end) }

  private val entryP = fromToP and sep and space and textP map { (ft, _, _, t) -> Pair(ft, t) }

  private val commentP = -lparen and textP and -rparen

  private val entryWithComment = entryP and optional(commentP) map { (entry, comment) ->
    val (begin, end) = entry.first
    val text = entry.second
    EntryModel(begin, end, text.trim(), comment.orEmpty().trim())
  }

  override val rootParser = entryWithComment

  fun parseLine(baseDate: LocalDate, line: String): SingleLineParseResult {
    if (line.trim().startsWith('#')) {
      return SingleLineParseResult(emptyList())
    }
    val res = tryParseToEnd(line)
    return when (res) {
      is Parsed -> {
        val model = res.value
        val baseTime = LocalDateTime.of(baseDate, LocalTime.now()).withHour(0).withMinute(0).truncatedTo(MINUTES)
        val begin = baseTime.withHour(model.begin.hour).withMinute(model.begin.minute)
        val end = baseTime.withHour(model.end.hour).withMinute(model.end.minute)
        LineParser.SingleLineParseResult(emptyList(), model.copy(begin = begin, end = end))
      }
      is ErrorResult -> {
        LineParser.SingleLineParseResult(listOf(ParseError(Severity.ERROR, 0, res.toString())))
      }
    }
  }
}
