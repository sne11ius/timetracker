package wi.co.timetracker.controller

import mu.KotlinLogging
import tornadofx.Controller
import tornadofx.getProperty
import tornadofx.observable
import tornadofx.property
import wi.co.timetracker.extensions.existsAndBlank
import wi.co.timetracker.extensions.formatDefault
import wi.co.timetracker.extensions.isWeekend
import wi.co.timetracker.model.MainModel
import wi.co.timetracker.model.MonthModel
import wi.co.timetracker.service.FileLoader
import java.time.Duration
import java.time.LocalDate

class MainController(lineNums: String = "", dayPart: String = "", weekPart: String = "", monthPart: String = "", summary: String = "", fiSummary: String = "", currentFiSummaryProject: String = "") : Controller() {

    private val logger = KotlinLogging.logger {}

    private val fileLoader: FileLoader by di()

    private val preferencesController: PreferencesController by inject()

    val mainModel = MainModel()

    private var lineNumbers: String by property(lineNums)
    fun lineNumbersProperty() = getProperty(MainController::lineNumbers)

    private var dayPart: String by property(dayPart)
    fun dayPartProperty() = getProperty(MainController::dayPart)

    private var weekPart: String by property(weekPart)
    fun weekPartProperty() = getProperty(MainController::weekPart)

    private var monthPart: String by property(monthPart)
    fun monthPartProperty() = getProperty(MainController::monthPart)

    private var summary: String by property(summary)
    fun summaryProperty() = getProperty(MainController::summary)

    private var fiSummary: String by property(fiSummary)
    fun fiSummaryProperty() = getProperty(MainController::fiSummary)

    private var currentFiSummaryProject: String by property(currentFiSummaryProject)
    fun currentFiSummaryProjectProperty() = getProperty(MainController::currentFiSummaryProject)

    val projectsInMonth = mutableListOf<String>().observable()

    var currentMonth: MonthModel? = null

    init {
        preferencesController.setOnPreferencesUpdatedListener(object : PreferencesController.OnPreferencesUpdatedListener {
            override fun onPreferencesUpdated() {
                reload(mainModel.currentDate)
            }
        })
        mainModel.fileContentProperty().addListener({ _, _, new ->
            mainModel.file.writeText(new)
            reload(mainModel.currentDate)
        })
        mainModel.currentDateProperty().addListener({ _, _, new ->
            reload(new)
        })

        currentFiSummaryProjectProperty().addListener({ _, _, new ->
            if (new != null) {
                val m = currentMonth
                if (null != m) {
                    this.fiSummary = with(preferencesController) {
                        m.getSummary(new, getBreakIndicators(), getTravelIndicators(), getTravelMultiplier(), getExcelCorrection())
                    }
                }
            } else this.fiSummary = ""
        })
        reload(LocalDate.now())
    }

    private fun reload(date: LocalDate) {
        with(preferencesController) {
            readDay(date, getBreakIndicators(), getTravelIndicators(), getTravelMultiplier())
            readWeek(date, getBreakIndicators(), getTravelIndicators(), getTravelMultiplier())
            readMonth(date, getBreakIndicators(), getTravelIndicators(), getTravelMultiplier())
        }
    }

    private fun readMonth(anyDayInMonth: LocalDate, breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float) {
        val month = fileLoader.loadMonth(anyDayInMonth, preferencesController.getBaseDir(), breakIndicators, travelIndicators, travelMultiplier)
        val oldSelection = currentFiSummaryProject
        projectsInMonth.clear()
        projectsInMonth.addAll(month.projectNames(preferencesController.getBreakIndicators()))
        if (projectsInMonth.contains(oldSelection)) {
            currentFiSummaryProject = oldSelection
        }
        val diff = month.workDurationDifference(breakIndicators, travelIndicators, travelMultiplier)
        monthPart = with(month) { mkShortSummary("Monat", expectedWorkDuration(), actualWorkDuration(breakIndicators, travelIndicators, travelMultiplier), diff) }
        currentMonth = month
    }

    private fun readWeek(anyDayInWeek: LocalDate, breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float) {
        val week = fileLoader.loadWeek(anyDayInWeek, preferencesController.getBaseDir(), breakIndicators, travelIndicators, travelMultiplier)
        val diff = week.workDurationDifference(breakIndicators, travelIndicators, travelMultiplier)
        weekPart = with(week) { mkShortSummary("Woche", expectedWorkDuration(), actualWorkDuration(breakIndicators, travelIndicators, travelMultiplier), diff) }
    }

    private fun readDay(day: LocalDate, breakIndicators: List<String>, travelIndicators: List<String>, travelMultiplier: Float) {
        val parseResult = fileLoader.loadDay(day, preferencesController.getBaseDir(), breakIndicators, travelIndicators, travelMultiplier)
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
        val diff = expectedDuration.minus(actualDuration)
        dayPart = mkShortSummary("Tag", expectedDuration, actualDuration, diff)
        mainModel.errors = parseResult.errors.fold("", { msg, (severity, line, message) ->
            msg + "${severity.toString().padEnd(5)} Zeile $line: $message\n"
        })
        summary = if (parseResult.dayModel != null) {
            parseResult.dayModel.toDaySummaryModel(breakIndicators, travelIndicators, travelMultiplier).toString()
        } else {
            ""
        }
    }

    private fun mkShortSummary(header: String, expected: Duration, actual: Duration, diff: Duration) = """
        |$header
        |Soll: ${expected.toHours()}
        |Ist: ${actual.formatDefault()}
        |Differenz: ${diff.formatDefault()}
        """.trimMargin()

}
