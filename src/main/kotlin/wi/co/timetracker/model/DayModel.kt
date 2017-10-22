package wi.co.timetracker.model

import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDate

data class DayModel(val date: LocalDate, private val entries: List<EntryModel>) {

    private val log = KotlinLogging.logger {}

    fun duration(excludes: List<String>, travelIndicators: List<String>, travelMultiplier: Float): Duration {
        return entries.fold(Duration.ZERO, { d, m ->
            d.plus(
                    if (excludes.any { m.text.contains(it) }) {
                        Duration.ZERO
                } else {
                        m.computeDuration(travelIndicators, travelMultiplier)
                }
            )
        })
    }

}
