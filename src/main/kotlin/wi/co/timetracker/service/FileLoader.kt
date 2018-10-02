package wi.co.timetracker.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import wi.co.timetracker.extensions.isWorkDay
import wi.co.timetracker.model.entry.DayModel
import wi.co.timetracker.model.entry.EntryModel
import wi.co.timetracker.model.entry.MonthModel
import wi.co.timetracker.model.entry.WeekModel
import wi.co.timetracker.model.parser.ParseError
import wi.co.timetracker.model.parser.ParseResult
import wi.co.timetracker.model.parser.Severity
import wi.co.timetracker.model.parser.info
import wi.co.timetracker.parser.LineParser
import java.io.File
import java.io.File.separator
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class FileLoader {

    private val log = KotlinLogging.logger {}

    @Autowired
    private lateinit var lineParser: LineParser

    fun loadDay(date: LocalDate, baseDir: File): ParseResult {
        log.debug { "Load data for $date from $baseDir" }
        val file = mkFile(date, baseDir)
        if (file.exists()) {
            if (file.readText().isBlank()) {
                log.debug { "File empty" }
                return ParseResult(file, listOf(info(0, "No file for this date")), null)
            }
            return loadDayFromFile(date, file)
        } else {
            log.debug { "No file" }
            return ParseResult(file, listOf(info(0, "No file for this date")), null)
        }
    }

    fun loadWeek(anyDayInWeek: LocalDate, baseDir: File): WeekModel {
        var day = anyDayInWeek
        while (day.dayOfWeek != DayOfWeek.MONDAY)
            day = day.minusDays(1)
        val entries = mutableListOf<DayModel>()
        for (index in 0 until 7) {
            val (_, _, dayModel) = loadDay(day, baseDir)
            if (null != dayModel) {
                entries += dayModel
            }
            day = day.plusDays(1)
        }
        return WeekModel(entries)
    }

    fun loadMonth(anyDayInMonth: LocalDate, baseDir: File): MonthModel {
        val firstDayOfMonth = anyDayInMonth.withDayOfMonth(1)
        var day = firstDayOfMonth
        val entries = mutableListOf<DayModel>()
        while (day.month == anyDayInMonth.month) {
            if (day.dayOfWeek.isWorkDay()) {
                val (_, _, dayModel) = loadDay(day, baseDir)
                if (null != dayModel) {
                    entries += dayModel
                }
            }
            day = day.plusDays(1)
        }
        return MonthModel(firstDayOfMonth, entries)
    }

    fun loadDay(file: File): ParseResult {
        val entries = mutableListOf<EntryModel>()
        val errors = mutableListOf<ParseError>()
        try {
            val date: LocalDate = LocalDate.parse(file.nameWithoutExtension.replace("Zeiten ", ""), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            file.readLines().forEachIndexed { index, it ->
                val line = index + 1
                if (!it.isBlank()) {
                    log.debug { "Reading index: $it" }
                    val parseResult = lineParser.parseLine(date, it)
                    for (err in parseResult.errors)
                        errors += ParseError(err.severity, line, "Column ${err.index + 1}: ${err.message}")
                    if (null != parseResult.entry)
                        entries += parseResult.entry
                }
            }
            val dayModel = DayModel(date, entries)
            return ParseResult(file, errors, dayModel)
        } catch (e: Exception) {
            return ParseResult(file, listOf(ParseError(Severity.ERROR, 0, "Could not parse file ${file.absolutePath}")), null)
        }
    }

    fun autoFixFiles(baseDir: File, dryRun: Boolean = true): Pair<Int, Int> {
        var totalFiles = 0
        var changedFiles = 0
        baseDir.walkTopDown()
                .filter { it.isFile }
                .filter {it.nameWithoutExtension.contains("Zeiten ")}
                .forEach { file ->
                    ++totalFiles
                    @Suppress("SimplifiableCallChain")
                    val fixedContent = file.readLines()
                            .filter { !it.trim().isBlank() && !it.trim().startsWith("=") }
                            .map { it
                                    .substringBeforeLast("=")
                                    .removePrefix("\uFEFF")
                                    .trim()}
                            .joinToString("\n") + if (file.readText().endsWith("\n")) "\n" else ""
                    if (fixedContent != file.readText()) {
                        changedFiles++
                        println("===================")
                        println("==> File ${file.absolutePath}")
                        println("==> Before:")
                        println(file.readText())
                        println("==> After:")
                        println(fixedContent)
                        if (!dryRun) {
                            file.writeText(fixedContent)
                        }
                    }
                }
        println("$changedFiles of $totalFiles files fixed")
        return Pair(changedFiles, totalFiles)
    }

    private fun loadDayFromFile(date: LocalDate, file: File): ParseResult {
        val entries = mutableListOf<EntryModel>()
        val errors = mutableListOf<ParseError>()
        file.readLines().forEachIndexed { index, it ->
            val line = index + 1
            if (!it.isBlank()) {
                log.debug { "Reading index: $it" }
                val parseResult = lineParser.parseLine(date, it)
                for (err in parseResult.errors)
                    errors += ParseError(err.severity, line, "Column ${err.index + 1}: ${err.message}")
                if (null != parseResult.entry)
                    entries += parseResult.entry
            }
        }
        val dayModel = DayModel(date, entries)
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
