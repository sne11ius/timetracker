package wi.co.timetracker.model

import java.time.Duration

data class WeekModel(private val entries: List<DayModel>) {

    fun workDurationDifference(breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float): Duration {
        return expectedWorkDuration().minus(actualWorkDuration(breakIndicators, travelIndicators, travelMultiplier))
    }

    fun expectedWorkDuration(): Duration = Duration.ofHours(40)

    fun actualWorkDuration(breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float): Duration {
        return entries.fold(Duration.ZERO, {d, m ->
            d.plus(m.duration(breakIndicators, travelIndicators, travelMultiplier))
        })
    }

}
