package wi.co.timetracker.controller

import mu.KotlinLogging
import tornadofx.Controller
import tornadofx.getProperty
import tornadofx.property
import wi.co.timetracker.model.MainModel
import wi.co.timetracker.service.PersistenceService
import java.io.File
import java.util.*

class MainController(lineNums: String = "") : Controller() {

    private val logger = KotlinLogging.logger {}

    private val persistenceService: PersistenceService by di()

    private val preferencesController: PreferencesController by inject()

    private val preferences = preferencesController.preferences

    val mainModel = MainModel()

    var lineNumbers by property(lineNums)
    fun lineNumbersProperty() = getProperty(MainController::lineNumbers)

    init {
        mainModel.fileContentProperty().addListener({ _, old, new ->
            if (null != mainModel.file) {
                mainModel.file.writeText(new)
                load(mainModel.currentDate)
            }
        })
        mainModel.currentDateProperty().addListener({ _, _, newValue ->
            println("newValue: $newValue")
            load(newValue)
        })
    }

    private fun load(newValue: Calendar) {
        val parseResult = persistenceService.loadData(newValue, File(preferences.baseDir))
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
        mainModel.errors = parseResult.errors.fold("", { msg, err ->
            msg + "\n${err.severity.toString().padEnd(5)} Zeile ${err.line}: ${err.message}"
        })
    }

}
