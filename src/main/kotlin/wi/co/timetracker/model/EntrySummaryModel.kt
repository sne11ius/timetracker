package wi.co.timetracker.model

import wi.co.timetracker.extensions.format
import wi.co.timetracker.extensions.formatDefault
import java.time.Duration
import java.time.LocalDate

data class EntrySummaryModel(val date: LocalDate, val text: String, val comment: String, val duration: Duration) {

    fun formatFiExcelStyle(excelCorrection: Duration): String {
        val theDate = date.format("dd.MM.yyyy")
        val begin = "09:00"
        val end = Duration.ZERO.plusHours(9).plus(duration).plus(excelCorrection).formatDefault()
        val what = text + if (comment.isNotBlank()) " ($comment)" else ""
        //     theDate        - empty - empty - empty - empty - begin    + end        - empty - empty - empty - what
        return theDate + "\t" + "\t" + "\t" + "\t" + "\t" + begin + "\t" + end + "\t " + "\t" + "\t" + "\t" + what
    }

}

