package wi.co.timetracker.controller

import javafx.collections.FXCollections
import mu.KotlinLogging
import tornadofx.Controller
import tornadofx.getProperty
import tornadofx.observable
import tornadofx.property
import wi.co.timetracker.model.SapProjectAssignment
import java.time.LocalDate

class AssignSapProjectsController(/*assignments: List<SapProjectAssignment> = mutableListOf<SapProjectAssignment>()*/) : Controller() {

    private val logger = KotlinLogging.logger {}

    val assignments = FXCollections.observableArrayList<SapProjectAssignment>()
    val availableSapProjects = mutableListOf<String>().observable()

    fun discard() {
        logger.debug { "Discardings these changes: $assignments" }
    }

    fun save() {
        logger.debug { "Saving these changes: $assignments" }
    }
}