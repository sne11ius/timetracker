package wi.co.timetracker.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import wi.co.timetracker.model.*
import java.io.File
import java.io.File.separator
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class PersistenceService {

    private val log = KotlinLogging.logger {}

    fun loadData(date: Calendar, baseDir: File): ParseResult {
        println("Load data for ${date.time} from $baseDir")
        val file = mkFile(date, baseDir)
        if (file.exists()) {
            if (file.readText().isBlank()) {
                println("File empty")
                return ParseResult(file, listOf(info(0, "No file for this date")), null)
            }
            return loadDayModel(date, file)
        } else {
            println("No file")
            return ParseResult(file, listOf(info(0, "No file for this date")), null)
        }
    }

    private fun loadDayModel(date: Calendar, file: File): ParseResult {
        val totalLines = file.readLines().size
        val entries = mutableListOf<EntryModel>()
        val errors = mutableListOf<ParseError>()
        var total: String? = null
        file.readLines().forEachIndexed { index, it ->
            val line = index + 1
            if (!it.isBlank()) {
                println("Reading line: $it")
                val minLength = 15
                if (/*index != totalLines - 1 && */it.length >= minLength) {
                    val fromTime = it.substring(0..4).trim()
                    val toTime = it.substring(8..12).trim()
                    log.debug { "from: $fromTime" }
                    log.debug { "to  : $toTime" }
                    if (fromTime.isBlank()) {
                        log.error { "Error: no from" }
                        errors += error(line, "No from")
                    }
                    if (toTime.isBlank()) {
                        log.error { "Error: no to" }
                        errors += error(line, "No to")
                    }
                    val comment = it.substring(15).trim()
                    log.debug { "Comment: $comment" }
                    val text = comment.substringBeforeLast("=").trim()
                    log.debug { "Text: $text" }
                    val entryModel = mkEntryModel(date, fromTime, toTime, text)
                    if (null != entryModel) {
                        if (comment.contains("=")) {
                            val sum = comment.substringAfterLast("=").trim()
                            log.debug { "sum: $sum" }
                            if (sum.isNotBlank()) {
                                val sumHours = sum.substringBefore(",")
                                val minutePart = if (sum.contains(",")) sum.substringAfter(",") else "0"
                                val minutes = "0.$minutePart".toFloat() * 60
                                try {
                                    val notedDuration = Duration
                                            .ofHours(if (sumHours.isBlank()) 0 else sumHours.toLong())
                                            .plusMinutes(minutes.toLong())
                                    if (entryModel.duration() != notedDuration) {
                                        val diff = entryModel.duration().minus(notedDuration).abs()
                                        val diffString = LocalTime.MIDNIGHT.plus(diff).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                                        log.warn { "Duration mismatch for $entryModel: $diffString" }
                                        errors += warn(line, "Duration mismatch of $diffString.")
                                    }
                                } catch (e: NumberFormatException) {
                                    errors += error(line, "Cannot parse sum")
                                }
                            } else {
                                errors += error(line, "Sum indicator but no sum.")
                            }
                        }
                        entries += entryModel
                    } else {
                        errors += error(line, "Cannot parse this line")
                    }
                } else if (index == totalLines - 1 && it.contains("==")) {
                    total = it.substringAfterLast("=").trim()
                    log.debug { "Total: $total" }
                } else {
                    log.warn { "Cannot parse line $index" }
                    errors += error(line, "Cannot parse line $index")
                    return ParseResult(file, errors, null)
                }
            }
        }
        val dayModel = DayModel(LocalDateTime
                .ofInstant(date.toInstant(), ZoneId.systemDefault())
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0), entries)
        if (total != null) {
            try {
                val totalHours = total!!.substringBefore(",")
                val minutePart = if (total!!.contains(",")) total!!.substringAfter(",") else "0"
                val minutes = "0.$minutePart".toFloat() * 60
                val notedDuration = Duration
                        .ofHours(totalHours.toLong())
                        .plusMinutes(minutes.toLong())
                val computedDuration = dayModel.duration("Essen", "Pause")
                if (computedDuration != notedDuration) {
                    log.warn { "Workday duration mismatch for $dayModel" }
                    log.warn { "Noted: $notedDuration" }
                    log.warn { "Computed: $computedDuration" }
                    val diff = computedDuration.minus(notedDuration).abs()
                    val diffString = LocalTime.MIDNIGHT.plus(diff).format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    errors += warn(file.readLines().size, "Workday duration mismatch of $diffString")
                }
            } catch (e: NumberFormatException) {
                errors += error(file.readLines().size, "Cannot parse total")
            }
        }
        return ParseResult(file, errors, dayModel)
    }

    private fun mkEntryModel(baseDate: Calendar, fromTime: String, toTime: String, text: String): EntryModel? {
        val begin = toLocalDateTime(baseDate, fromTime) ?: return null
        val end = toLocalDateTime(baseDate, toTime) ?: return null
        return EntryModel(begin, if (end.isBefore(begin)) end.plusDays(1) else end, text)
    }

    private fun toLocalDateTime(baseDate: Calendar, time: String): LocalDateTime? {
        if (!time.contains(":")) {
            return null
        }
        try {
            val hours = time.substringBefore(":").trim().toInt()
            val minutes = time.substringAfter(":").trim().toInt()
            return LocalDateTime
                    .ofInstant(baseDate.toInstant(), ZoneId.systemDefault())
                    .withHour(hours)
                    .withMinute(minutes)
                    .withSecond(0)
                    .withNano(0)
        } catch (e: NumberFormatException) {
            log.debug { "Cannot parse time from $time" }
            return null
        }
    }

    private fun mkFile(calendar: Calendar, baseDir: File): File {
        val date = calendar.time
        val year = SimpleDateFormat("yyyy").format(date)
        val yearAndMonth = SimpleDateFormat("yyyy-MM").format(date)
        val full = SimpleDateFormat("yyyy-MM-dd").format(date)
        val filename = "Zeiten $full.txt"
        val file = File(baseDir, "$year$separator$yearAndMonth$separator$filename")
        println("Load file $file")
        return file
    }
}
