package wi.co.timetracker.controller

import mu.KotlinLogging
import tornadofx.Controller
import tornadofx.getProperty
import tornadofx.observable
import tornadofx.property
import wi.co.timetracker.model.SapProjectAssignment

class AssignSapProjectsController(
        saveDisabled: Boolean = false
) : Controller() {

    private val logger = KotlinLogging.logger {}

    val none = "Klicken zum Ändern"

    var mappingComplete: Boolean = false

    val assignments = mutableListOf<SapProjectAssignment>().observable()
    val availableSapProjects = mutableListOf<String>().observable()

    var saveDisabled by property(saveDisabled)
    val saveDisabledProperty = getProperty(AssignSapProjectsController::saveDisabled)

    fun discard() {
        logger.debug { "Discardings these changes: $assignments" }
    }

    fun updateMappingComplete() {
        saveDisabled = assignments.any { it.sapProjectNameProperty.get() == none }
        mappingComplete = !saveDisabled
    }

    fun reset(projectNames: Collection<String>, sapProjectNames: List<String>) {
        saveDisabled = true
        mappingComplete = false
        availableSapProjects.clear()
        availableSapProjects.addAll(sapProjectNames.sorted())
        assignments.clear()
        assignments.addAll(projectNames.sorted().map { SapProjectAssignment(it, none) })
        updateMappingComplete()
    }

    fun getMapping(): Map<String, String> {
        return assignments.fold(mutableMapOf<String, String>(), { map, ass ->
            val new = mutableMapOf<String, String>()
            new.putAll(map)
            new.put(ass.userProjectNameProperty.get(), ass.sapProjectNameProperty.get())
            new
        })
    }

}
