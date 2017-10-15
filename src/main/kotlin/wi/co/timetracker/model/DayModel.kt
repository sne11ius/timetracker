package wi.co.timetracker.model

import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime

data class DayModel(val date: LocalDateTime, val entries: List<EntryModel>) {

    private val log = KotlinLogging.logger {}

    fun duration(vararg excludes: String): Duration {
        return entries.fold(Duration.ZERO, { d, m ->
            if (!excludes.any { m.text.contains(it, true) }) {
                d.plus(m.duration())
            } else {
                log.debug { "Skipping entry: $m" }
                d
            }
        })
    }

}
