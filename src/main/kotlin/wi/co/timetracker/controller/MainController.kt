package wi.co.timetracker.controller

import mu.KotlinLogging
import tornadofx.Controller
import tornadofx.getProperty
import tornadofx.property
import wi.co.timetracker.model.MainModel
import wi.co.timetracker.service.PersistenceService
import java.io.File
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate

class MainController(lineNums: String = "", dayPart: String = "", weekPart: String = "", monthPart: String = "") : Controller() {

    private val logger = KotlinLogging.logger {}

    private val persistenceService: PersistenceService by di()

    private val preferencesController: PreferencesController by inject()

    private val preferences = preferencesController.preferences

    val mainModel = MainModel()

    var lineNumbers by property(lineNums)
    fun lineNumbersProperty() = getProperty(MainController::lineNumbers)

    var dayPart by property(dayPart)
    fun dayPartProperty() = getProperty(MainController::dayPart)

    var weekPart by property(weekPart)
    fun weekPartProperty() = getProperty(MainController::weekPart)

    var monthPart by property(monthPart)
    fun monthPartProperty() = getProperty(MainController::monthPart)

    init {
        preferencesController.setOnPreferencesUpdatedListener(object : PreferencesController.OnPreferencesUpdatedListener {
            override fun onPreferencesUpdated() {
                reload(mainModel.currentDate, preferencesController.getBreakIndicators())
            }
        })
        mainModel.fileContentProperty().addListener({ _, _, new ->
            if (null != mainModel.file) {
                mainModel.file.writeText(new)
                reload(mainModel.currentDate, preferencesController.getBreakIndicators())
            }
        })
        mainModel.currentDateProperty().addListener({ _, _, new ->
            reload(new, preferencesController.getBreakIndicators())
        })
        reload(LocalDate.now(), preferencesController.getBreakIndicators())
    }

    private fun reload(date: LocalDate, breakIndicators: List<String>) {
        readDay(date, breakIndicators)
        readWeek(date, breakIndicators)
        readMonth(date, breakIndicators)
    }

    private fun readMonth(anyDayInMonth: LocalDate, breakIndicators: List<String>) {
        var day = anyDayInMonth.withDayOfMonth(1)
        var exptectedWorkTime = Duration.ZERO
        var actualWorkTime = Duration.ZERO
        while (day.month == anyDayInMonth.month) {
            logger.debug { day }
            if (day.dayOfWeek != DayOfWeek.SATURDAY && day.dayOfWeek != DayOfWeek.SUNDAY) {
                exptectedWorkTime = exptectedWorkTime.plusHours(8)
                val (_, _, dayModel) = persistenceService.loadData(day, baseDir(), breakIndicators)
                if (null != dayModel) {
                    actualWorkTime = actualWorkTime.plus(dayModel.duration(breakIndicators))
                }
            }
            day = day.plusDays(1)
        }
        //val diff = exptectedWorkTime.minus(actualWorkTime).abs()
        monthPart = "Monat\nSoll: ${exptectedWorkTime.toHours()}\nIst: ${actualWorkTime.toHours()}"
    }

    private fun readWeek(anyDayInWeek: LocalDate, breakIndicators: List<String>) {
        var day = anyDayInWeek
        while (day.dayOfWeek != DayOfWeek.MONDAY)
            day = day.minusDays(1)
        var exptectedWorkTime = Duration.ZERO
        var actualWorkTime = Duration.ZERO
        while (day.dayOfWeek != DayOfWeek.MONDAY || day.dayOfMonth <= anyDayInWeek.dayOfMonth) {
            logger.debug { day }
            if (day.dayOfWeek != DayOfWeek.SATURDAY && day.dayOfWeek != DayOfWeek.SUNDAY) {
                exptectedWorkTime = exptectedWorkTime.plusHours(8)
                val (_, _, dayModel) = persistenceService.loadData(day, baseDir(), breakIndicators)
                if (null != dayModel) {
                    actualWorkTime = actualWorkTime.plus(dayModel.duration(breakIndicators))
                }
            }
            day = day.plusDays(1)
        }
        //val diff = exptectedWorkTime.minus(actualWorkTime).abs()
        weekPart = "Woche\nSoll: ${exptectedWorkTime.toHours()}\nIst: ${actualWorkTime.toHours()}"
    }

    private fun readDay(day: LocalDate, breakIndicators: List<String>) {
        val parseResult = persistenceService.loadData(day, baseDir(), breakIndicators)
        lineNumbers = if (parseResult.file.exists()) {
            (1..parseResult.file.readLines().size).fold("", { str, index ->
                "$str$index\n"
            })
        } else ""
        mainModel.file = parseResult.file
        mainModel.fileContent = if (parseResult.file.exists()) parseResult.file.readText() else ""
        if (parseResult.file.exists() && parseResult.file.readText().isBlank()) {
            parseResult.file.delete()
        }
        mainModel.dayModel = parseResult.dayModel
        val actualDuration = if (parseResult.dayModel != null) parseResult.dayModel.duration(breakIndicators) else Duration.ZERO
        val expedtedDuration = if (day.dayOfWeek == DayOfWeek.SATURDAY || day.dayOfWeek == DayOfWeek.SUNDAY) Duration.ZERO else Duration.ZERO.plusHours(8)
        dayPart = "Tag\nSoll: ${expedtedDuration.toHours()}\nIst: ${actualDuration.toHours()}"
        mainModel.errors = parseResult.errors.fold("", { msg, (severity, line, message) ->
            msg + "${severity.toString().padEnd(5)} Zeile $line: $message\n"
        })
    }

    private fun baseDir(): File = File(preferences.baseDir)

}
