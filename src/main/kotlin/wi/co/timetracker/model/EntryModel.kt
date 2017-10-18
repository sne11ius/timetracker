package wi.co.timetracker.model

import java.time.Duration
import java.time.LocalDateTime

data class EntryModel(val begin: LocalDateTime, val end: LocalDateTime, val text: String, val comment: String = "") {

    fun duration(): Duration {
        return Duration.between(begin, end)
    }

}
