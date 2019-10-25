package wi.co.timetracker.model.summary

import java.time.LocalDate
import wi.co.timetracker.extensions.formatDecimal

data class DaySummaryModel(val day: LocalDate, val entries: List<EntrySummaryModel>) {

    override fun toString(): String {
        return entries.fold("") { s, (_, text, comments, total) ->
            s + "$text${if (comments.isNotBlank()) " ($comments)" else ""} = ${total.formatDecimal(2)}\n"
        }.trim()
    }
}
