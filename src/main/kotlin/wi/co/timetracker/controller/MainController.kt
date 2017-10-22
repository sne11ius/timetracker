package wi.co.timetracker.controller

import mu.KotlinLogging
import tornadofx.Controller
import tornadofx.getProperty
import tornadofx.property
import wi.co.timetracker.extensions.existsAndBlank
import wi.co.timetracker.extensions.isWeekend
import wi.co.timetracker.extensions.isWorkDay
import wi.co.timetracker.model.MainModel
import wi.co.timetracker.service.PersistenceService
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate

class MainController(lineNums: String = "", dayPart: String = "", weekPart: String = "", monthPart: String = "", summary: String = "") : Controller() {

    private val logger = KotlinLogging.logger {}

    private val persistenceService: PersistenceService by di()

    private val preferencesController: PreferencesController by inject()

    val mainModel = MainModel()

    var lineNumbers by property(lineNums)
    fun lineNumbersProperty() = getProperty(MainController::lineNumbers)

    var dayPart by property(dayPart)
    fun dayPartProperty() = getProperty(MainController::dayPart)

    var weekPart by property(weekPart)
    fun weekPartProperty() = getProperty(MainController::weekPart)

    var monthPart by property(monthPart)
    fun monthPartProperty() = getProperty(MainController::monthPart)

    var summary by property(summary)
    fun summaryProperty() = getProperty(MainController::summary)

    init {
        preferencesController.setOnPreferencesUpdatedListener(object : PreferencesController.OnPreferencesUpdatedListener {
            override fun onPreferencesUpdated() {
                reload(mainModel.currentDate)
            }
        })
        mainModel.fileContentProperty().addListener({ _, _, new ->
            if (null != mainModel.file) {
                mainModel.file.writeText(new)
                reload(mainModel.currentDate)
            }
        })
        mainModel.currentDateProperty().addListener({ _, _, new ->
            reload(new)
        })
        reload(LocalDate.now())
    }

    private fun reload(date: LocalDate) {
        readDay(date, preferencesController.getBreakIndicators(), preferencesController.getTravelIndicators(), preferencesController.getTravelMultiplier())
        readWeek(date, preferencesController.getBreakIndicators(), preferencesController.getTravelIndicators(), preferencesController.getTravelMultiplier())
        readMonth(date, preferencesController.getBreakIndicators(), preferencesController.getTravelIndicators(), preferencesController.getTravelMultiplier())
    }

    private fun readMonth(anyDayInMonth: LocalDate, breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float) {
        var day = anyDayInMonth.withDayOfMonth(1)
        var exptectedWorkTime = Duration.ZERO
        var actualWorkTime = Duration.ZERO
        while (day.month == anyDayInMonth.month) {
            logger.debug { day }
            if (day.dayOfWeek != DayOfWeek.SATURDAY && day.dayOfWeek != DayOfWeek.SUNDAY) {
                exptectedWorkTime = exptectedWorkTime.plusHours(8)
                val (_, _, dayModel) = persistenceService.loadData(day, preferencesController.getBaseDir(), breakIndicators, travelIndicators, travelMultiplier)
                if (null != dayModel) {
                    actualWorkTime = actualWorkTime.plus(dayModel.duration(breakIndicators, travelIndicators, travelMultiplier))
                }
            }
            day = day.plusDays(1)
        }
        // val diff = exptectedWorkTime.minus(actualWorkTime)
        monthPart = "Monat\nSoll: ${exptectedWorkTime.toHours()}\nIst: ${actualWorkTime.toHours()}"//\nDifferennz: $diff"
    }

    private fun readWeek(anyDayInWeek: LocalDate, breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float) {
        var day = anyDayInWeek
        while (day.dayOfWeek != DayOfWeek.MONDAY)
            day = day.minusDays(1)
        var exptectedWorkTime = Duration.ZERO
        var actualWorkTime = Duration.ZERO
        for (index in 0 until 7) {
            logger.debug { "$day: ${day.dayOfWeek}" }
            if (day.dayOfWeek.isWorkDay()) {
                exptectedWorkTime = exptectedWorkTime.plusHours(8)
                val (_, _, dayModel) = persistenceService.loadData(day, preferencesController.getBaseDir(), breakIndicators, travelIndicators, travelMultiplier)
                if (null != dayModel) {
                    actualWorkTime = actualWorkTime.plus(dayModel.duration(breakIndicators, travelIndicators, travelMultiplier))
                }
            }
            day = day.plusDays(1)
        }
        //val diff = exptectedWorkTime.minus(actualWorkTime).abs()
        weekPart = "Woche\nSoll: ${exptectedWorkTime.toHours()}\nIst: ${actualWorkTime.toHours()}"
    }

    private fun readDay(day: LocalDate, breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float) {
        val parseResult = persistenceService.loadData(day, preferencesController.getBaseDir(), breakIndicators, travelIndicators, travelMultiplier)
        lineNumbers = if (parseResult.file.exists()) {
            (1..parseResult.file.readLines().size).fold("", { str, index ->
                "$str$index\n"
            })
        } else ""
        mainModel.file = parseResult.file
        mainModel.fileContent = if (parseResult.file.exists()) parseResult.file.readText() else ""
        if (parseResult.file.existsAndBlank()) {
            parseResult.file.delete()
        }
        mainModel.dayModel = parseResult.dayModel
        val actualDuration = if (parseResult.dayModel != null) parseResult.dayModel.duration(breakIndicators, travelIndicators, travelMultiplier) else Duration.ZERO
        val expectedDuration = if (day.dayOfWeek.isWeekend()) Duration.ZERO else Duration.ofHours(8)
        dayPart = "Tag\nSoll: ${expectedDuration.toHours()}\nIst: ${actualDuration.toHours()}"
        mainModel.errors = parseResult.errors.fold("", { msg, (severity, line, message) ->
            msg + "${severity.toString().padEnd(5)} Zeile $line: $message\n"
        })
    }

}
