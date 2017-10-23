package wi.co.timetracker.model

import wi.co.timetracker.extensions.getExpectedWorkDuration
import wi.co.timetracker.extensions.isWorkDay
import java.time.Duration
import java.time.LocalDate

data class MonthModel(private val firstDayOfMonth: LocalDate, val entries: List<DayModel>) {

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

}
