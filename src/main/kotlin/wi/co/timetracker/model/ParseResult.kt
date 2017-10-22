package wi.co.timetracker.model

import java.io.File

data class ParseResult(val file: File, val errors: List<ParseError>, val dayModel: DayModel?)
