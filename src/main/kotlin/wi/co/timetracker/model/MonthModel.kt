package wi.co.timetracker.model

import wi.co.timetracker.extensions.getExpectedWorkDuration
import wi.co.timetracker.extensions.isWorkDay
import java.time.Duration
import java.time.LocalDate

data class MonthModel(private val firstDayOfMonth: LocalDate, private val entries: List<DayModel>) {

    fun projectNames(): List<String> {
        return entries.flatMap { it.entries }.map { it.text }.toSet().sorted()
    }

    fun workDurationDifference(breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float): Duration {
        return expectedWorkDuration().minus(actualWorkDuration(breakIndicators, travelIndicators, travelMultiplier))
    }

    fun expectedWorkDuration(): Duration {
        var expectedWorkDuration = Duration.ZERO
        var currentDay = firstDayOfMonth
        while (currentDay.month == firstDayOfMonth.month) {
            expectedWorkDuration = expectedWorkDuration.plus(currentDay.dayOfWeek.getExpectedWorkDuration())
            currentDay = currentDay.plusDays(1)
        }
        return expectedWorkDuration
    }

    fun actualWorkDuration(breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float): Duration {
        return entries.fold(Duration.ZERO, { d, m ->
            if (m.date.dayOfWeek.isWorkDay())
                d.plus(m.duration(breakIndicators, travelIndicators, travelMultiplier))
            else d
        })
    }

    fun getSummary(projectName: String, breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float): String {
        val daySummaries = entries.map { it.toDaySummaryModel(breakIndicators, travelIndicators, travelMultiplier) }
        val entrySummaryModels = daySummaries.map { it.entries.firstOrNull { it.text == projectName } }
        return entrySummaryModels.fold("", { s, m ->
            if (null != m) {
                "$s${m.formatFiExcelStyle()}\n"
            } else s
        }).trim()
    }

}
