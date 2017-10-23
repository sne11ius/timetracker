package wi.co.timetracker.model

import wi.co.timetracker.extensions.isWorkDay
import java.time.Duration

data class WeekModel(val entries: List<DayModel>) {

    fun workDurationDifference(breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float): Duration {
        return expectedWorkDuration().minus(actualWorkDuration(breakIndicators, travelIndicators, travelMultiplier))
    }

    fun expectedWorkDuration(): Duration = Duration.ofHours(40)

    fun actualWorkDuration(breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float): Duration {
        return entries.fold(Duration.ZERO, {d, m ->
            if (m.date.dayOfWeek.isWorkDay())
                d.plus(m.duration(breakIndicators, travelIndicators, travelMultiplier))
            else d
        })
    }

}