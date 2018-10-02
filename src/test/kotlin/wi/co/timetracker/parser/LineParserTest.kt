package wi.co.timetracker.parser

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.StringSpec
import wi.co.timetracker.model.entry.EntryModel
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class LineParserTest : StringSpec() {
    init {
        val parser = LineParser()

        "Should parse stuff" {
            val baseTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)

            parser.parseToEnd("01:15 - 13:30: project") shouldBe EntryModel(
                    baseTime.withHour(1).withMinute(15),
                    baseTime.withHour(13).withMinute(30),
                    "project",
                    ""
            )
            parser.parseToEnd("01:15 - 13:30: project (comment)") shouldBe EntryModel(
                    baseTime.withHour(1).withMinute(15),
                    baseTime.withHour(13).withMinute(30),
                    "project",
                    "comment"
            )
            parser.parseToEnd("23:23 - 13:30: project 1 (comment)") shouldBe EntryModel(
                    baseTime.withHour(23).withMinute(23),
                    baseTime.withHour(13).withMinute(30),
                    "project 1",
                    "comment"
            )
            parser.parseToEnd("12:15 - 19:59: project (comment 1)") shouldBe EntryModel(
                    baseTime.withHour(12).withMinute(15),
                    baseTime.withHour(19).withMinute(59),
                    "project",
                    "comment 1"
            )
            parser.parseToEnd("12:15 - 13:30: project ßü+äö#ä 1 (comß´öä#0ment 1)") shouldBe EntryModel(
                    baseTime.withHour(12).withMinute(15),
                    baseTime.withHour(13).withMinute(30),
                    "project ßü+äö#ä 1",
                    "comß´öä#0ment 1"
            )
        }
    }
}
