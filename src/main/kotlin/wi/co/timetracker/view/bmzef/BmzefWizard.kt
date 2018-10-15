package wi.co.timetracker.view.bmzef

import javafx.scene.control.ListView
import mu.KotlinLogging
import tornadofx.*
import wi.co.timetracker.extensions.checked
import wi.co.timetracker.extensions.isChecked
import wi.co.timetracker.extensions.isUnchecked
import wi.co.timetracker.extensions.unchecked
import wi.co.timetracker.model.bmzef.ActivityPath
import wi.co.timetracker.service.mbzef.BmzefService

var entriesListView: ListView<String>? = null

class BmzefWizard: Wizard("Bmzef all the things!") {
  private val logger = KotlinLogging.logger {}

  private val service: BmzefService by inject()

  private val model: BmzefWizardData by inject()

  init {
    val enterprises = service.readAvailableEnterprises()
    add(SelectDateRange::class)
    add(CheckAssignments::class)
    model.enterpriseTitles.setAll(enterprises.map { it.title }.sorted())
    model.selectedEnterpriseProperty().onChange { newEnterprise ->
      logger.debug { "New enterprise: $newEnterprise" }
      val enterprise = enterprises.first { it.title == newEnterprise }
      model.contractTitles.setAll(enterprise.contracts.map { it.title }.sorted())
      model.kindTitles.clear()
      model.activityTitles.clear()
      selectionChanged()
    }
    model.selectedContractProperty().onChange { newContract ->
      if (newContract != null) {
        val enterprise = enterprises.first { it.title == model.selectedEnterprise }
        val contract = enterprise.contracts.first { it.title == newContract }
        model.kindTitles.setAll(contract.kinds.map { it.title }.sorted())
        model.activityTitles.clear()
      }
      selectionChanged()
    }
    model.selectedKindProperty().onChange { newKind ->
      if (newKind != null) {
        val enterprise = enterprises.first { it.title == model.selectedEnterprise }
        val contract = enterprise.contracts.first { it.title == model.selectedContract }
        val kind = contract.kinds.first { it.title == newKind }
        model.activityTitles.setAll(kind.activities.map { it.title }.sorted())
      }
      selectionChanged()
    }
    model.selectedActivityProperty().onChange { _ ->
      selectionChanged()
    }

    model.selectedEntryTextProperty().onChange { newValue ->
      logger.debug { "Selected text: $newValue" }
    }
  }

  private fun selectionChanged() {
    logger.debug { "Selected entry text: ${model.selectedEntryText}" }
    val selectedPath: ActivityPath = with(model) {
      ActivityPath.Path(selectedEnterprise, selectedContract, selectedKind, selectedActivity)
    }
    logger.debug { selectedPath }
    if (model.selectedEntryText != null) {
      if (service.isValid(selectedPath)) {
        logger.debug { "Path seems valid." }
        if (model.selectedEntryText.isUnchecked) {
          val newEntryText = model.selectedEntryText.checked
          model.entryTexts.remove(model.selectedEntryText)
          model.entryTexts.add(newEntryText)
          model.entryTexts.sort()
          model.selectedEntryText = newEntryText
          entriesListView!!.selectionModel.select(newEntryText)
        }
        model.projectMapping = model.projectMapping.run {
          copy(
            mappedEntries = mappedEntries.filter { it.entryText != model.selectedEntryText.unchecked }.toSet() + BmzefService.EntryMapping(model.selectedEntryText.unchecked, selectedPath),
            unmappedEntries = unmappedEntries - model.selectedEntryText.unchecked
          )
        }
      } else {
        logger.debug { "Path is not valid." }
        if (model.selectedEntryText.isChecked) {
          val newEntryText = model.selectedEntryText.unchecked
          model.entryTexts.remove(model.selectedEntryText)
          model.entryTexts.add(newEntryText)
          model.entryTexts.sort()
          model.selectedEntryText = newEntryText
          entriesListView!!.selectionModel.select(newEntryText)
        }
        model.projectMapping = model.projectMapping.run {
          copy(
            mappedEntries = mappedEntries.filter { it.entryText != model.selectedEntryText }.toSet(),
            unmappedEntries = unmappedEntries + model.selectedEntryText
          )
        }
      }
    }
    if (model.projectMapping.isComplete) {
      logger.debug { "Mapping is complete" }
    }
    isComplete = model.projectMapping.isComplete
  }

  override val canFinish = allPagesComplete
  override val canGoNext = currentPageComplete

}
