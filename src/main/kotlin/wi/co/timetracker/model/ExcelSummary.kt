package wi.co.timetracker.model

import java.time.Duration

data class ExcelSummary(val date: String, val time: String, val description: String, val totalDuration: Duration)