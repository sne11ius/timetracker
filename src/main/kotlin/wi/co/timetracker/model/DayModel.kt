package wi.co.timetracker.model

import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDate

data class DayModel(val date: LocalDate, private val entries: List<EntryModel>) {

    private val log = KotlinLogging.logger {}

    fun duration(excludes: List<String>, travelIndicators: List<String>, travelMultiplier: Float): Duration {
        return entries.fold(Duration.ZERO, { d, m ->
            if (!excludes.any { m.text.contains(it, true) }) {
                if (travelIndicators.any { m.text.contains(it, true) }) {
                    val scaledDuration = Duration.ofMillis((m.duration().toMillis() * travelMultiplier).toLong())
                    d.plus(scaledDuration)
                } else {
                    d.plus(m.duration())
                }
            } else {
                log.debug { "Skipping entry: $m" }
                d
            }
        })
    }

}
