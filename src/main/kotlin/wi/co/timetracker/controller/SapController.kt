package wi.co.timetracker.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import mu.KotlinLogging
import tornadofx.Controller
import tornadofx.getProperty
import tornadofx.property
import wi.co.timetracker.extensions.formatDefault
import wi.co.timetracker.model.DaySummaryModel
import wi.co.timetracker.model.Severity
import wi.co.timetracker.sap.SapControl
import wi.co.timetracker.service.FileLoader
import wi.co.timetracker.view.AssignSapProjectsView
import java.io.File
import java.time.LocalDate
import java.util.*


class SapController(
        dateBegin: LocalDate = LocalDate.now(),
        dateEnd: LocalDate = LocalDate.now(),
        selectedDuration: String = ""
) : Controller() {

    private val logger = KotlinLogging.logger {}

    private val fileLoader: FileLoader by di()

    private val preferencesController: PreferencesController by inject()

    private val assignSapProjectsController: AssignSapProjectsController by inject()

    private var dateBegin: LocalDate by property(dateBegin)
    fun dateBeginProperty() = getProperty(SapController::dateBegin)

    private var dateEnd: LocalDate by property(dateEnd)
    fun dateEndProperty() = getProperty(SapController::dateEnd)

    private var selectedDuration: String by property(selectedDuration)
    fun selectedDurationProperty() = getProperty(SapController::selectedDuration)

    init {
        dateBeginProperty().addListener({ _, _, _ ->
            updateDurationLabel()
        })
        dateEndProperty().addListener({ _, _, _ ->
            updateDurationLabel()
        })
    }

    private fun updateDurationLabel() {
        selectedDuration = "${dateBegin.formatDefault()} - ${dateEnd.formatDefault()}"
    }

    private fun getProjectMapping(): Map<String, String> {
        var result: Map<String, String>? = null
        preferences(PreferencesController.PREFS_NAME) {
            val pref = get(PROJECT_MAPPING, "{}")
            val mapper = ObjectMapper()
            val typeRef = object : TypeReference<HashMap<String, String>>() {}
            result = mapper.readValue(pref, typeRef)
        }
        return result!!
    }

    private fun setProjectMapping(mapping: Map<String, String>) {
        preferences(PreferencesController.PREFS_NAME) {
            val mapper = ObjectMapper()
            put(PROJECT_MAPPING, mapper.writeValueAsString(mapping))
        }
    }

    fun fillSapGui() {
        logger.debug { dateBegin }
        logger.debug { dateEnd }
        val summaries = mutableListOf<DaySummaryModel>()
        var currentDate = dateBegin
        while (currentDate != dateEnd.plusDays(1)) {
            logger.debug { "Now at " + currentDate }
            val parseResult = with(preferencesController) { fileLoader.loadDay(currentDate, getBaseDir(), getBreakIndicators(), getTravelIndicators(), getTravelMultiplier()) }
            val errors = parseResult.errors.fold("", { msg, (severity, line, message) ->
                if (severity != Severity.INFO)
                    msg + "${severity.toString().padEnd(5)} Zeile $line: $message\n"
                else msg
            })
            if (errors.isNotBlank()) {
                Alert(Alert.AlertType.ERROR, "Die Daten im gewählten Zeitraum enthalten Fehler:\n$errors").show()
                return
            }

            if (parseResult.dayModel != null) {
                summaries += with(preferencesController) { parseResult.dayModel.toDaySummaryModel(getBreakIndicators(), getTravelIndicators(), getTravelMultiplier()) }
            }
            currentDate = currentDate.plusDays(1)
        }
        if (summaries.isEmpty()) {
            Alert(Alert.AlertType.INFORMATION, "Keine Einträge in diesem Zeitraum.").show()
            return
        }
        logger.debug { "Adding to SAP:" }
        summaries.forEach({ s ->
            logger.debug { "Summary for ${s.day}: ${s.entries}" }
        })
        val firstDay = summaries.first().day
        val mapping = getProjectMapping()
        val projects = summaries.flatMap { it.entries }.map { it.text }.toSet()
        val missing = mutableListOf<String>()
        projects.forEach {
            if (!mapping.containsKey(it)) {
                logger.debug { "Missing mapping for a $it" }
                missing += it
            }
        }
        if (missing.isNotEmpty()) {
            var msg = "Nicht alle Einträge konnten SAP-Projekten zugewiesen werden:\n\n"
            missing.forEach { msg += " - " + it + "\n" }
            msg += "\nSoll im SAP nach Einträgen gesucht werden?"
            val result = Alert(Alert.AlertType.CONFIRMATION, msg).showAndWait()
            if (result.isPresent) {
                if (result.get() == ButtonType.OK) {
                    logger.debug { "yap" }
                    val tempFilename = projects.joinToString(PROJECT_SEPARATOR).hashCode().toString()
                    val cacheFile = File(preferencesController.getBaseDir(), "$tempFilename.timetracker.sapcache.txt")
                    val sapProjectNames =
                            if (cacheFile.exists()) {
                                logger.debug { "Skipping this step since we found a cache file @ $cacheFile" }
                                val names = cacheFile.readText().split(PROJECT_SEPARATOR).map { it.trim() }
                                logger.debug { "Found sap project names: $names" }
                                names
                            } else {
                                with(preferencesController.preferences) {
                                    primaryStage.isIconified = true
                                    val names = SapControl.findAllProjectNames(sapUsername, sapPassword, firstDay)
                                    logger.debug { "Project names: $names" }
                                    cacheFile.writeText(names.joinToString(PROJECT_SEPARATOR))
                                    primaryStage.isIconified = false
                                    primaryStage.toFront()
                                    names
                                }
                            }
                    assignSapProjectsController.reset(projects, sapProjectNames)
                    find(AssignSapProjectsView::class).openModal(block = true)
                    if (assignSapProjectsController.mappingComplete) {
                        val createdMapping: Map<String, String> = assignSapProjectsController.getMapping()
                        val mergedMapping = mutableMapOf<String, String>()
                        mergedMapping.putAll(mapping)
                        mergedMapping.putAll(createdMapping)
                        logger.debug {
                            "Merged mapping: \n" + mergedMapping.map { (k, v) ->
                                "$k -> $v"
                            }.joinToString("\n")
                        }
                        setProjectMapping(mergedMapping)
                    } else {
                        logger.debug { "Mapping not complete" }
                    }
                } else {
                    logger.debug { "nope" }
                }
            }
        }
    }

    companion object {
        val PROJECT_MAPPING = "projectMapping"
        val PROJECT_SEPARATOR = "<|>"
    }
}
