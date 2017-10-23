package wi.co.timetracker.model

import java.time.Duration
import java.time.LocalDate

data class EntrySummaryModel(val date: LocalDate, val text: String, val comment: String, val duration: Duration)
