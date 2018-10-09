package wi.co.timetracker.model.parser

import wi.co.timetracker.model.entry.DayModel
import java.io.File

data class ParseResult(val file: File, val errors: List<ParseError>, val dayModel: DayModel?)

val List<ParseError>.hasErrors
  get() = any { it.severity == Severity.ERROR }
