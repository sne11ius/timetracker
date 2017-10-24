package wi.co.timetracker.model

import wi.co.timetracker.extensions.format
import java.time.Duration
import java.time.LocalDate

data class EntrySummaryModel(val date: LocalDate, val text: String, val comment: String, val duration: Duration) {

    fun formatFiExcelStyle(): String {
        val theDate = date.format("dd.MM.yyyy")
        val begin = "09:00"
        val end = "10:00"
        val what = text + if (comment.isNotBlank()) " ($comment)" else ""
        //     theDate - empty - empty - empty - empty - begin        - end - empty - empty - empty - what
        return theDate + "\t" + "\t" + "\t" + "\t" + begin + "\t" + end + "\t" + "\t" + "\t" + what
    }

}

