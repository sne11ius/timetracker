package wi.co.timetracker.controller

import javafx.scene.control.Alert
import tornadofx.*
import wi.co.timetracker.extensions.checked
import wi.co.timetracker.model.parser.hasErrors
import wi.co.timetracker.model.summary.DaySummaryModel
import wi.co.timetracker.service.FileLoader
import wi.co.timetracker.service.mbzef.BmzefService
import wi.co.timetracker.view.bmzef.BmzefWizardData

class BmzefWizardController : Controller() {
  private val service: BmzefService by inject()
  private val model: BmzefWizardData by inject()
  private val preferencesController: PreferencesController by inject()
  private val fileLoader: FileLoader by di()

  fun reloadEntries() {
    if (model.beginDate.isAfter(model.endDate)) {
      val end = model.endDate
      model.endDate = model.beginDate
      model.beginDate = end
    }
    val begin = model.beginDate
    val end = model.endDate
    var currentDay = begin
    var models = listOf<DaySummaryModel>()
    while (currentDay != end.plusDays(1)) {
      val (_, errors, entry) = fileLoader.loadDay(currentDay, preferencesController.baseDir)
      if (errors.hasErrors) {
        Alert(Alert.AlertType.ERROR, "Datei für $currentDay ist leider kaputt.").showAndWait()
        throw RuntimeException()
      }
      if (entry != null) {
        models += with (preferencesController) {
          entry.toDaySummaryModel(
            breakIndicators,
            travelIndicators,
            travelMultiplier
          )
        }
      }
      currentDay = currentDay.plusDays(1)
    }
    models = models
      .map { m -> m.copy(entries = m.entries.filter { entry -> preferencesController.bmzefIgnoreIndicators.none { it == entry.text } }) }
      .filter { m -> m.entries.isNotEmpty() }
      .toMutableList()
    val allEntries = models.flatMap { it.entries }.map { it.text }.toSet()
    model.projectMapping *= service.loadMapping(allEntries)
    with (model) {
      entryTexts.setAll(allEntries.map { if (projectMapping.unmappedEntries.contains(it)) it else it.checked }.toList().sorted())
    }
  }

  fun updateMappingCache() {

  }
}
