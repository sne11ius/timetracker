package wi.co.timetracker.extensions

import org.apache.commons.lang3.time.DurationFormatUtils
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate

fun String.toFile(): File = File(this)

fun File.existsAndBlank(): Boolean = this.exists() && this.readText().isBlank()

fun DayOfWeek.isWeekend(): Boolean = this == DayOfWeek.SATURDAY || this == DayOfWeek.SUNDAY
fun DayOfWeek.isWorkDay(): Boolean = !isWeekend()
fun DayOfWeek.getExpectedWorkDuration(): Duration = if (this.isWorkDay()) Duration.ofHours(8) else Duration.ZERO

// fun LocalDate.formatDefault(): String = format("dd.MM.YYYY")
fun LocalDate.format(format: String): String = format(java.time.format.DateTimeFormatter.ofPattern(format))

fun Duration.formatDefault(): String {
    return if (this.isNegative) {
        DurationFormatUtils.formatDuration(this.abs().toMillis(), "-HH:mm")
    } else {
        DurationFormatUtils.formatDuration(this.toMillis(), "HH:mm")
    }
}

fun Duration.formatDecimal(roundDigits: Int): String {
    val base = BigDecimal.valueOf(this.toMillis())
    val millisPerHour = BigDecimal.valueOf(Duration.ofHours(1).toMillis())
    return base.divide(millisPerHour, 5 * roundDigits, RoundingMode.UP).setScale(roundDigits, RoundingMode.CEILING).toFloat().toString().replace(".", ",")
}

fun Duration.toDouble(roundDigits: Int): Double {
    return formatDecimal(roundDigits).replace(",", ".").toDouble()
}
