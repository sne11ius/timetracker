package wi.co.timetracker.model

import java.time.Duration
import java.time.LocalDateTime

data class EntryModel(
        private val begin: LocalDateTime,
        private val end: LocalDateTime,
        val text: String,
        val comment: String = ""
) {

    fun computeDuration(travelIndicators: List<String>, travelMultiplier: Float): Duration {
        return if (travelIndicators.any { text.contains(it) || comment.contains(it) }) {
            Duration.ofMillis((computeBaseDuration().toMillis() * travelMultiplier).toLong())
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
