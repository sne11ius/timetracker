package wi.co.timetracker.view

import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.control.Alert
import jfxtras.scene.control.LocalDatePicker
import mu.KotlinLogging
import tornadofx.*
import wi.co.timetracker.controller.PreferencesController
import wi.co.timetracker.model.bmzef.ActivityPath
import wi.co.timetracker.model.parser.hasErrors
import wi.co.timetracker.model.summary.DaySummaryModel
import wi.co.timetracker.service.FileLoader
import wi.co.timetracker.service.mbzef.BmzefService
import java.time.LocalDate

class BmzefWizardData(
  beginDate: LocalDate = LocalDate.now().minusDays(1),
  endDate: LocalDate = LocalDate.now(),
  enterpriseTitles: List<String> = emptyList(),
  contractTitles: List<String> = emptyList(),
  kindTitles: List<String> = emptyList(),
  activityTitles: List<String> = emptyList(),
  entryTexts: List<String> = emptyList(),
  selectedEntryText: String? = null,
  selectedEnterprise: String? = null,
  selectedContract: String? = null,
  selectedKind: String? = null,
  selectedActivity: String? = null
) {
  var beginDate: LocalDate by property(beginDate)
  fun beginDateProperty() = getProperty(BmzefWizardData::beginDate)

  var endDate: LocalDate by property(endDate)
  fun endDateProperty() = getProperty(BmzefWizardData::endDate)

  var enterpriseTitles: ObservableList<String> by property(FXCollections.observableArrayList(enterpriseTitles))
  fun enterpriseTitlesProperty() = getProperty(BmzefWizardData::enterpriseTitles)

  var contractTitles: ObservableList<String> by property(FXCollections.observableArrayList(contractTitles))
  fun contractTitlesProperty() = getProperty(BmzefWizardData::contractTitles)

  var kindTitles: ObservableList<String> by property(FXCollections.observableArrayList(kindTitles))
  fun kindTitlesProperty() = getProperty(BmzefWizardData::kindTitles)

  var activityTitles: ObservableList<String> by property(FXCollections.observableArrayList(activityTitles))
  fun activityTitlesProperty() = getProperty(BmzefWizardData::activityTitles)

  var entryTexts: ObservableList<String> by property(FXCollections.observableArrayList(entryTexts))
  fun entryTextsProperty() = getProperty(BmzefWizardData::entryTexts)

  var selectedEntryText: String by property(selectedEntryText)
  fun selectedEntryTextProperty() = getProperty(BmzefWizardData::selectedEntryText)

  var selectedEnterprise: String by property(selectedEnterprise)
  fun selectedEnterpriseProperty() = getProperty(BmzefWizardData::selectedEnterprise)

  var selectedContract: String by property(selectedContract)
  fun selectedContractProperty() = getProperty(BmzefWizardData::selectedContract)

  var selectedKind: String by property(selectedKind)
  fun selectedKindProperty() = getProperty(BmzefWizardData::selectedKind)

  var selectedActivity: String by property(selectedActivity)
  fun selectedActivityProperty() = getProperty(BmzefWizardData::selectedActivity)
}

private val model = BmzefWizardData()

class SelectDateRange: View("Zeitraum wählen") {
  private val preferencesController: PreferencesController by inject()
  private val service: BmzefService by inject()
  private val fileLoader: FileLoader by di()

  override fun onSave() {
    if (model.beginDate.isAfter(model.endDate)) {
      val end = model.endDate
      model.endDate = model.beginDate
      model.beginDate = end
    }
    val begin = model.beginDate
    val end = model.endDate
    var currentDay = begin
    val models = mutableListOf<DaySummaryModel>()
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
    val allEntries = models.flatMap { it.entries }.map { it.text }.toSet()
    val projectMapping = service.loadMapping(allEntries)
    model.entryTexts.setAll(allEntries.toList().sorted())
  }

  override val root = form {
    fieldset(title) {
      field("Start Datum") {
        this += LocalDatePicker().apply {
          prefWidth = 250.0
          allowNull = false
          mode = LocalDatePicker.Mode.SINGLE
          Bindings.bindBidirectional(localDateProperty(), model.beginDateProperty())
        }
      }
      field("End Datum") {
        this += LocalDatePicker().apply {
          prefWidth = 250.0
          allowNull = false
          mode = LocalDatePicker.Mode.SINGLE
          Bindings.bindBidirectional(localDateProperty(), model.endDateProperty())
        }
      }
    }
  }
}

class CheckAssignments: View("Zuordnungen Prüfen") {
  override val root = borderpane {
    // padding = tornadofx.insets(10)
    top = label("Bitte ordne jeder von dir selbst aufgezeichneten Tätigkeit eine Tätigkeit aus der bmzef zu.") {
      padding = tornadofx.insets(5)
    }
    center = hbox {
      vbox {
        label("Eigener Name")
        listview(model.entryTexts).bindSelected(model.selectedEntryTextProperty())
      }
      vbox {
        label("Vorhaben")
        listview(model.enterpriseTitles).bindSelected(model.selectedEnterpriseProperty())
      }
      vbox {
        label("Vertrag")
        listview(model.contractTitles).bindSelected(model.selectedContractProperty())
      }
      vbox {
        label("Tätigkeitsart")
        listview(model.kindTitles).bindSelected(model.selectedKindProperty())
      }
      vbox {
        label("Tätigkeit")
        listview(model.activityTitles).bindSelected(model.selectedActivityProperty())
      }
    }
  }
}

class BmzefWizard: Wizard("Bmzef all the things!") {
  private val logger = KotlinLogging.logger {}

  private val service: BmzefService by inject()

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
    }
    model.selectedContractProperty().onChange { newContract ->
      if (newContract != null) {
        val enterprise = enterprises.first { it.title == model.selectedEnterprise }
        val contract = enterprise.contracts.first { it.title == newContract }
        model.kindTitles.setAll(contract.kinds.map { it.title }.sorted())
        model.activityTitles.clear()
      }
    }
    /*
    with (model) {
      listOf(
        selectedEnterpriseProperty(),
        selectedContractProperty(),
        selectedKindProperty(),
        selectedActivityProperty()
      ).forEach { it.onChange { _ -> selectionChanged() } }
    }
    */
  }

  private fun selectionChanged() {
    val selectedPath: ActivityPath = with (model) {
      ActivityPath.Path(selectedEnterprise, selectedContract, selectedKind, selectedActivity)
    }
    logger.debug { selectedPath }
    if (service.isValid(selectedPath)) {
      logger.debug { "Path seems valid." }
    } else {
      logger.debug { "Path is not valid." }
    }
  }

  override val canFinish = allPagesComplete
  override val canGoNext = currentPageComplete
}
