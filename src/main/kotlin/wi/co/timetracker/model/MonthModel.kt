package wi.co.timetracker.model

import wi.co.timetracker.extensions.getExpectedWorkDuration
import wi.co.timetracker.extensions.isWorkDay
import java.time.Duration
import java.time.LocalDate

data class MonthModel(private val firstDayOfMonth: LocalDate, private val entries: List<DayModel>) {

    fun projectNames(breakIndicators: List<String>): List<String> {
        return entries.flatMap { it.entries }.map { it.text }.toSet().filter { !breakIndicators.any { indicator -> it.contains(indicator) } }.sorted()
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

    fun getExcelSummary(projectName: String, breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float, excelCorrection: Duration): ExcelSummary {
        val daySummaries = entries.map { it.toDaySummaryModel(breakIndicators, travelIndicators, travelMultiplier) }
        val entrySummaryModels = daySummaries.map { it.entries.firstOrNull { it.text == projectName } }
        val summary = entrySummaryModels.fold(ExcelSummary("", "", "", Duration.ZERO), { (date, time, description, duration), m ->
            if (null != m) {
                val (currentDate, currentTime, currentDescription, currentDuration) = m.formatFiExcelStyle(excelCorrection)
                ExcelSummary(date + currentDate + "\n", time + currentTime + "\n", description + currentDescription + "\n", duration.plus(currentDuration))
            } else ExcelSummary(date, time, description, duration)
        })
        return with (summary) {
            ExcelSummary(date.trim(), time.trim(), description.trim(), totalDuration)
        }
    }

    fun getProjectDurations(breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float): Map<String, Duration> {
        return projectNames(breakIndicators).map { projectName ->
            val daySummaries = entries.map { it.toDaySummaryModel(breakIndicators, travelIndicators, travelMultiplier) }
            val entrySummaryModels = daySummaries.map { it.entries.firstOrNull { it.text == projectName } }
            val totalDuration = entrySummaryModels.fold(Duration.ZERO, {d, m ->
                if (null != m) {
                    d.plus(m.duration)
                } else d
            })
            projectName to totalDuration
        }.toMap()
    }

}
