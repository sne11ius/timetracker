package wi.co.timetracker.extensions

import java.io.File
import java.time.DayOfWeek

fun String.toFile(): File = File(this)

fun DayOfWeek.isWeekend(): Boolean = this == DayOfWeek.SATURDAY || this == DayOfWeek.SUNDAY
fun DayOfWeek.isWorkDay(): Boolean = !isWeekend()

fun File.existsAndBlank(): Boolean = this.exists() && this.readText().isBlank()
