package wi.co.timetracker.model

import wi.co.timetracker.extensions.formatDecimal

data class DaySummaryModel(private val entries: List<EntrySummaryModel>) {

    override fun toString(): String {
        return entries.fold("", { s, (text, comments, total) ->
            s + "$text${if (comments.isNotBlank()) " ($comments)" else ""} = ${total.formatDecimal(2)}\n"
        }).trim()
    }

}
