package wi.co.timetracker.model

import java.time.Duration
import java.time.LocalDateTime

data class EntryModel(private val begin: LocalDateTime, private val end: LocalDateTime, val text: String, val comment: String = "") {

    fun duration(): Duration {
        return if (end.isAfter(begin))
            Duration.between(begin, end)
        else
            Duration.between(begin, end.plusDays(1))
    }

}
