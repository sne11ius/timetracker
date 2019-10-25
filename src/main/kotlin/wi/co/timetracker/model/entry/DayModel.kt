package wi.co.timetracker.model.entry

import java.time.Duration
import java.time.LocalDate
import wi.co.timetracker.model.summary.DaySummaryModel
import wi.co.timetracker.model.summary.EntrySummaryModel

data class DayModel(val date: LocalDate, val entries: List<EntryModel>) {

    fun duration(excludes: List<String>, travelIndicators: List<String>, travelMultiplier: Float): Duration {
        return entries.fold(Duration.ZERO) { d, m ->
            d.plus(if (excludes.any { m.text.contains(it) }) {
                Duration.ZERO
            } else {
                m.computeDuration(travelIndicators, travelMultiplier)
            })
        }
    }

    fun toDaySummaryModel(breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float): DaySummaryModel {
        return DaySummaryModel(date, entries
                .asSequence()
                .filter { !breakIndicators.any { indicator -> it.text.contains(indicator) } }
                .groupBy { it.text }
                .map { (text, entries) ->
                    val totalDuration = entries.fold(Duration.ZERO) { d, m ->
                        d.plus(m.computeDuration(travelIndicators, travelMultiplier))
                    }
                    var anyEmpty = false
                    var anyNotEmpty = false
                    val comments = entries.map { it.comment }.toSet().sorted().fold("") { string, comment ->
                        if (comment.isNotBlank()) {
                            anyNotEmpty = true
                            "$string$comment, "
                        } else {
                            anyEmpty = true
                            string
                        }
                    }.removeSuffix(", ")
                    EntrySummaryModel(date, text, (if (anyEmpty && anyNotEmpty) "u.a. " else "") + comments, totalDuration)
                }
                .sortedBy { it.text }
                .toList()
        )
    }
}
