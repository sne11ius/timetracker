package wi.co.timetracker.model

import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime

data class EntryModel(private val begin: LocalDateTime, private val end: LocalDateTime, val text: String, val notedDuration: Duration? = null, val comment: String = "") {

    private val log = KotlinLogging.logger {}

    fun computeDurationDifference(travelIndicators: List<String>, travelMultiplier: Float): Duration {
        return if (null == notedDuration)
            Duration.ZERO
        else
            notedDuration.minus(computeDuration(travelIndicators, travelMultiplier)).abs()
    }

    fun computeDuration(travelIndicators: List<String>, travelMultiplier: Float): Duration {
        return if (travelIndicators.any { text.contains(it, true) }) {
            val adjustedDuration = Duration.ofMillis((computeBaseDuration().toMillis() * travelMultiplier).toLong())
            val roundedDuration = Duration.ofMinutes(adjustedDuration.toMinutes())
            if (roundedDuration != notedDuration) {
                log.warn { "not equal: $adjustedDuration, $notedDuration" }
            }
            roundedDuration
        } else
            computeBaseDuration()
    }

    private fun computeBaseDuration(): Duration {
        return if (end.isAfter(begin))
            Duration.between(begin, end)
        else
            Duration.between(begin, end.plusDays(1))
    }

}
