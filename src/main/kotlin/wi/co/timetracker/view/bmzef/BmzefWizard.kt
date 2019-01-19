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
import wi.co.timetracker.model.bmzef.ActivityPathPart
import wi.co.timetracker.model.bmzef.sortedTitles
import wi.co.timetracker.service.mapper
import wi.co.timetracker.service.mbzef.BmzefService

var entriesListView: ListView<String>? = null
var enterprisesListView: ListView<String>? = null
var contractsListView: ListView<String>? = null
var kindsListView: ListView<String>? = null
var activitiesListView: ListView<String>? = null

class BmzefWizard : Wizard("Bmzef all the things!") {

  private val logger = KotlinLogging.logger {}
  private val bmzefService: BmzefService by inject()
  private val model: BmzefWizardData by inject()
  private var updateSelection = true

  init {
    add(SelectDateRange::class)
    add(CheckAssignments::class)

    model.selectedEnterpriseProperty().onChange { newEnterprise ->
      if (newEnterprise != null) {
        val enterprise = model.avalailabledEnterprises.first { it.title == newEnterprise }
        model.contractTitles.setAll(enterprise.contracts.sortedTitles())
        model.kindTitles.clear()
        model.activityTitles.clear()
      }
      selectionChanged()
    }
    model.selectedContractProperty().onChange { newContract ->
      if (newContract != null) {
        val enterprise = model.avalailabledEnterprises.first { it.title == model.selectedEnterprise }
        val contract = enterprise.contracts.first { it.title == newContract }
        model.kindTitles.setAll(contract.kinds.sortedTitles())
        model.activityTitles.clear()
      }
      selectionChanged()
    }
    model.selectedKindProperty().onChange { newKind ->
      if (newKind != null) {
        val enterprise = model.avalailabledEnterprises.first { it.title == model.selectedEnterprise }
        val contract = enterprise.contracts.first { it.title == model.selectedContract }
        val kind = contract.kinds.first { it.title == newKind }
        model.activityTitles.setAll(kind.activities.sortedTitles())
      }
      selectionChanged()
    }
    model.selectedActivityProperty().onChange { selectionChanged() }

    with(model) {
      selectedEntryTextProperty().onChange { selectedValue ->
        if (!updateSelection)
          return@onChange
        val newValue = selectedValue?.unchecked
        val path = projectMapping.pathFor(newValue)
        when (path) {
          is ActivityPath.NoPath -> {
            Platform.runLater {
              selectedEnterprise = enterpriseTitles.first()
            }
          }
          is ActivityPath.Path -> {
            val (e, c, k, a) = path
            Platform.runLater {
              // see https://stackoverflow.com/a/27623202/649835
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
    selectionChanged()
  }

  private fun selectionChanged() {
    val selectedPath = model.selectedPath
    if (selectedPath is ActivityPath.Path) {
      @Suppress("SENSELESS_COMPARISON") // "Wir können hier Niemandem trauen"
      if (model.selectedEntryText != null) {
        if (bmzefService.isValid(selectedPath, model.avalailabledEnterprises)) {
          if (model.selectedEntryText.isUnchecked) {
            checkSelectedEntry()
          }
          model.projectMapping += Pair(model.selectedEntryText.unchecked, selectedPath)
        } else {
          if (model.selectedEntryText.isChecked) {
            uncheckSelectedEntry()
          }
          model.projectMapping -= model.selectedEntryText
        }
      }
    }

    find(CheckAssignments::class).complete.value = model.projectMapping.isComplete
  }

  private fun checkSelectedEntry() {
    ignoringSelection {
      val newEntryText = model.selectedEntryText.checked
      model.entryTexts.remove(model.selectedEntryText)
      model.entryTexts.add(newEntryText)
      model.entryTexts.sort()
      model.selectedEntryText = newEntryText
      entriesListView!!.selectionModel.select(newEntryText)
    }
  }

  private fun uncheckSelectedEntry() {
    ignoringSelection {
      val newEntryText = model.selectedEntryText.unchecked
      model.entryTexts.remove(model.selectedEntryText)
      model.entryTexts.add(newEntryText)
      model.entryTexts.sort()
      model.selectedEntryText = newEntryText
      entriesListView!!.selectionModel.select(newEntryText)
    }
  }

  override fun onSave() {
    bmzefService.updateProjectMappingCache(model.projectMapping)
    logger.debug { mapper.writeValueAsString(model.projectMapping) }
    bmzefService.commit(model.daySummaryModels, model.projectMapping)
    close()
  }

  fun reset(enterprises: Set<ActivityPathPart.Enterprise>) {
    ignoringSelection {
      model.reset(enterprises)
    }
  }

  private fun ignoringSelection(f: () -> Unit) {
    updateSelection = false
    f()
    updateSelection = true
  }

  override val canFinish = allPagesComplete
  override val canGoNext = currentPageComplete

}
