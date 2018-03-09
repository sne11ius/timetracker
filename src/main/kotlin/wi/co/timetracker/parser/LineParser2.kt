package wi.co.timetracker.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import org.springframework.stereotype.Component
import wi.co.timetracker.model.EntryModel
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MINUTES

@Component
class LineParser2 : Grammar<EntryModel>() {

    private val hours by token("[01][0-9]|2[0-3]")
    private val minutes by token("[0-5][0-9]")
    private val sep by token(":")
    private val fromToSep by token(" - ")
    private val space by token(" ")
    private val lparen by token("\\(")
    private val rparen by token("\\)")
    private val text by token("(\\w|\\s|:)+")

    private val hoursP = hours use { text.toInt() }
    private val minutesP by (hours or minutes) use { text.toInt() }
    private val textP = text use { text }

    val time
            = hoursP and sep and minutesP
    private val timeP = time map { (h, _, m) -> LocalDateTime.now().truncatedTo(MINUTES).withHour(h).withMinute(m) }
    private val fromTo
            = timeP and fromToSep and timeP
    private val fromToP = fromTo map { (begin, _, end) -> Pair(begin, end) }

    private val entry
            = fromToP and sep and space and textP map { (ft, _, _, t) -> Pair(ft, t) }

    private val comment
            = -lparen and textP and -rparen

    private val entryWithComment
            = entry and optional(comment) map { (e, c) ->
        val (begin, end) = e.first
        val text = e.second
        EntryModel(begin, end, text.trim(), null, c.orEmpty().trim())
    }

    override val rootParser = entryWithComment
}
