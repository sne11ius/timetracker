package wi.co.timetracker.model.entry

import java.time.Duration
import java.time.Duration.between
import java.time.Duration.ofMillis
import java.time.LocalDateTime

data class EntryModel(
  val begin: LocalDateTime,
  val end: LocalDateTime,
  val text: String,
  val comment: String = ""
) {

    fun computeDuration(travelIndicators: List<String>, travelMultiplier: Float): Duration {
        return if (travelIndicators.any { text.contains(it) || comment.contains(it) }) {
            ofMillis((computeBaseDuration().toMillis() * travelMultiplier).toLong())
        } else
            computeBaseDuration()
    }

    private fun computeBaseDuration(): Duration {
        return if (end.isAfter(begin))
            between(begin, end)
        else
            between(begin, end.plusDays(1))
    }
}
