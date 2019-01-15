package wi.co.timetracker.view.bmzef

import javafx.application.Platform
import javafx.scene.control.ListView
import mu.KotlinLogging
import tornadofx.*
import wi.co.timetracker.extensions.checked
import wi.co.timetracker.extensions.isChecked
import wi.co.timetracker.extensions.isUnchecked
import wi.co.timetracker.extensions.unchecked
import wi.co.timetracker.model.bmzef.ActivityPath
import wi.co.timetracker.model.bmzef.sortedTitles
import wi.co.timetracker.service.mbzef.BmzefService

var entriesListView: ListView<String>? = null
var enterprisesListView: ListView<String>? = null
var contractsListView: ListView<String>? = null
var kindsListView: ListView<String>? = null
var activitiesListView: ListView<String>? = null

class BmzefWizard: Wizard("Bmzef all the things!") {

  private val logger = KotlinLogging.logger {}

  private val service: BmzefService by inject()

  private val model: BmzefWizardData by inject()

  private var updateSelection = true

  init {
    add(SelectDateRange::class)
    add(CheckAssignments::class)

    val enterprises = model.avalailabledEnterprises
    model.enterpriseTitles.setAll(enterprises.sortedTitles())
    model.selectedEnterpriseProperty().onChange { newEnterprise ->
      logger.debug { "New enterprise: $newEnterprise" }
      val enterprise = enterprises.first { it.title == newEnterprise }
      model.contractTitles.setAll(enterprise.contracts.sortedTitles())
      model.kindTitles.clear()
      model.activityTitles.clear()
      selectionChanged()
    }
    model.selectedContractProperty().onChange { newContract ->
      if (newContract != null) {
        val enterprise = enterprises.first { it.title == model.selectedEnterprise }
        val contract = enterprise.contracts.first { it.title == newContract }
        model.kindTitles.setAll(contract.kinds.sortedTitles())
        model.activityTitles.clear()
      }
      selectionChanged()
    }
    model.selectedKindProperty().onChange { newKind ->
      if (newKind != null) {
        val enterprise = enterprises.first { it.title == model.selectedEnterprise }
        val contract = enterprise.contracts.first { it.title == model.selectedContract }
        val kind = contract.kinds.first { it.title == newKind }
        model.activityTitles.setAll(kind.activities.sortedTitles())
      }
      selectionChanged()
    }
    model.selectedActivityProperty().onChange { _ ->
      selectionChanged()
    }

    with (model) {
      selectedEntryTextProperty().onChange { selectedValue ->
        if (!updateSelection)
          return@onChange
        val newValue = selectedValue?.unchecked
        logger.debug { "Selected text: $newValue" }
        val path = projectMapping.pathFor(newValue)
        enterprisesListView
        when (path) {
          is ActivityPath.NoPath -> {
            Platform.runLater {
              selectedEnterprise = enterpriseTitles.first()
            }
          }
          is ActivityPath.Path -> {
            val (e, c, k, a) = path
            Platform.runLater { // see https://stackoverflow.com/a/27623202/649835
              selectedEnterprise = e
              if (c != null)
                selectedContract = c
              if (k != null)
                selectedKind = k
              if (a != null)
                selectedActivity = a
            }
          }
        }
      }
    }
  }

  private fun selectionChanged() {
    logger.debug { "Selected entry text: ${model.selectedEntryText}" }
    val selectedPath: ActivityPath = with(model) {
      val enterprise = selectedEnterprise
      if (null == enterprise)
        ActivityPath.NoPath
      else
      ActivityPath.Path(
        enterprise,
        selectedContract,
        selectedKind,
        selectedActivity
      )
    }
    logger.debug { selectedPath }
    when (selectedPath) {
      is ActivityPath.NoPath -> {
        logger.debug { "This is not a valid path :D" }
      }
      is ActivityPath.Path -> {
        if (model.selectedEntryText != null) {
          if (service.isValid(selectedPath, model.avalailabledEnterprises)) {
            logger.debug { "Path seems valid." }
            if (model.selectedEntryText.isUnchecked) {
              updateSelection = false
              val newEntryText = model.selectedEntryText.checked
              model.entryTexts.remove(model.selectedEntryText)
              model.entryTexts.add(newEntryText)
              model.entryTexts.sort()
              model.selectedEntryText = newEntryText
              entriesListView!!.selectionModel.select(newEntryText)
              updateSelection = true
            }
            model.projectMapping = model.projectMapping.run {
              copy(
                mappedEntries = mappedEntries
                  .filter { it.entryText != model.selectedEntryText.unchecked }
                  .toSet() + BmzefService.EntryMapping(model.selectedEntryText.unchecked, selectedPath),
                unmappedEntries = unmappedEntries - model.selectedEntryText.unchecked
              )
            }
          } else {
            logger.debug { "Path is not valid." }
            if (model.selectedEntryText.isChecked) {
              updateSelection = false
              val newEntryText = model.selectedEntryText.unchecked
              model.entryTexts.remove(model.selectedEntryText)
              model.entryTexts.add(newEntryText)
              model.entryTexts.sort()
              model.selectedEntryText = newEntryText
              entriesListView!!.selectionModel.select(newEntryText)
              updateSelection = true
            }
            model.projectMapping = model.projectMapping.run {
              copy(
                mappedEntries = mappedEntries.filter { it.entryText != model.selectedEntryText }.toSet(),
                unmappedEntries = unmappedEntries + model.selectedEntryText
              )
            }
          }
        }
      }
    }

    if (model.projectMapping.isComplete) {
      logger.debug { "Mapping is complete" }
    }
    isComplete = model.projectMapping.isComplete
  }

  override fun onSave() {
    logger.debug { "onSave" }
    service.updateProjectMappingCache(model.projectMapping)
  }

  // override val canFinish = allPagesComplete
  override val canFinish = isComplete.toProperty()
  override val canGoNext = currentPageComplete

}
