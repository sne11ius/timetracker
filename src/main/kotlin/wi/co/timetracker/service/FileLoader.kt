package wi.co.timetracker.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import wi.co.timetracker.extensions.isWorkDay
import wi.co.timetracker.model.*
import wi.co.timetracker.parser.LineParser
import java.io.File
import java.io.File.separator
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Service
class FileLoader {

    private val log = KotlinLogging.logger {}

    @Autowired
    lateinit private var lineParser: LineParser

    fun loadDay(date: LocalDate, baseDir: File, breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float): ParseResult {
        log.debug { "Load data for $date from $baseDir" }
        val file = mkFile(date, baseDir)
        if (file.exists()) {
            if (file.readText().isBlank()) {
                log.debug { "File empty" }
                return ParseResult(file, listOf(info(0, "No file for this date")), null)
            }
            return loadDayFromFile(date, file, breakIndicators, travelIndicators, travelMultiplier)
        } else {
            log.debug { "No file" }
            return ParseResult(file, listOf(info(0, "No file for this date")), null)
        }
    }

    fun loadWeek(anyDayInWeek: LocalDate, baseDir: File, breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float): WeekModel {
        var day = anyDayInWeek
        while (day.dayOfWeek != DayOfWeek.MONDAY)
            day = day.minusDays(1)
        val entries = mutableListOf<DayModel>()
        for (index in 0 until 7) {
            val (_, _, dayModel) = loadDay(day, baseDir, breakIndicators, travelIndicators, travelMultiplier)
            if (null != dayModel) {
                entries += dayModel
            }
            day = day.plusDays(1)
        }
        return WeekModel(entries)
    }

    fun loadMonth(anyDayInMonth: LocalDate, baseDir: File, breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float): MonthModel {
        var day = anyDayInMonth.withDayOfMonth(1)
        val entries = mutableListOf<DayModel>()
        while (day.month == anyDayInMonth.month) {
            if (day.dayOfWeek.isWorkDay()) {
                val (_, _, dayModel) = loadDay(day, baseDir, breakIndicators, travelIndicators, travelMultiplier)
                if (null != dayModel) {
                    entries += dayModel
                }
            }
            day = day.plusDays(1)
        }
        return MonthModel(day, entries)
    }

    private fun loadDayFromFile(date: LocalDate, file: File, breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float): ParseResult {
        val totalLines = file.readLines().size
        val entries = mutableListOf<EntryModel>()
        val errors = mutableListOf<ParseError>()
        var total: String? = null
        file.readLines().forEachIndexed { index, it ->
            val line = index + 1
            if (!it.isBlank()) {
                log.debug { "Reading index: $it" }
                if (index == totalLines - 1 && it.startsWith("=")) {
                    total = it.substringAfterLast("=").trim()
                } else {
                    val parseResult = lineParser.parseLine(date, it)
                    for (err in parseResult.errors)
                        errors += ParseError(err.severity, line, "Column ${err.index + 1}: ${err.message}")
                    if (null != parseResult.entry)
                        entries += parseResult.entry
                }
            }
        }
        val dayModel = DayModel(date, entries)

        if (total != null) {
            try {
                val totalHours = total!!.substringBefore(",")
                val minutePart = if (total!!.contains(",")) total!!.substringAfter(",") else "0"
                val minutes = "0.$minutePart".toFloat() * 60
                val secondsPart = minutes.toString().substringAfter(".")
                val seconds = "0.${if (secondsPart.isBlank()) "0" else secondsPart}".toFloat() * 60
                val notedDuration = Duration
                        .ofHours(totalHours.toLong())
                        .plusMinutes(minutes.toLong())
                        .plusSeconds(seconds.toLong())
                val computedDuration = dayModel.duration(breakIndicators, travelIndicators, travelMultiplier)
                if (computedDuration != notedDuration) {
                    log.warn { "Workday computeDuration mismatch for $dayModel" }
                    log.warn { "Noted: $notedDuration" }
                    log.warn { "Computed: $computedDuration" }
                    val diff = computedDuration.minus(notedDuration).abs()
                    val diffString = LocalTime.MIDNIGHT.plus(diff).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    errors += warn(file.readLines().size, "Workday mismatch of $diffString")
                }
            } catch (e: NumberFormatException) {
                errors += error(file.readLines().size, "Cannot parse total")
            }
        }
        return ParseResult(file, errors, dayModel)
    }

    private fun mkFile(date: LocalDate, baseDir: File): File {
        val year = date.format(DateTimeFormatter.ofPattern("yyyy"))
        val yearAndMonth = date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        val full = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        val filename = "Zeiten $full.txt"
        val file = File(baseDir, "$year$separator$yearAndMonth$separator$filename")
        log.debug { "Load file $file" }
        return file
    }
}
