package wi.co.timetracker.model

import java.time.Duration

data class EntrySummaryModel(val text: String, val comment: String, val duration: Duration)
