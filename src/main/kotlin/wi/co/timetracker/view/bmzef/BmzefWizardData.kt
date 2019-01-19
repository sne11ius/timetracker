package wi.co.timetracker.view.bmzef

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import wi.co.timetracker.model.bmzef.ActivityPath
import wi.co.timetracker.model.bmzef.ActivityPathPart
import wi.co.timetracker.model.bmzef.ProjectMapping
import wi.co.timetracker.model.bmzef.sortedTitles
import wi.co.timetracker.model.summary.DaySummaryModel
import java.time.LocalDate

class BmzefWizardData(
  avalailabledEnterprises: Set<ActivityPathPart.Enterprise> = emptySet(),
  projectMapping: ProjectMapping = ProjectMapping(),
  daySummaryModels: List<DaySummaryModel> = emptyList(),
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
) : ViewModel() {
  var projectMapping: ProjectMapping by property(projectMapping)
  var daySummaryModels: List<DaySummaryModel> by property(daySummaryModels)
  var avalailabledEnterprises: Set<ActivityPathPart.Enterprise> by property(avalailabledEnterprises)
  var beginDate: LocalDate by property(beginDate)
  fun beginDateProperty() = getProperty(BmzefWizardData::beginDate)
  var endDate: LocalDate by property(endDate)
  fun endDateProperty() = getProperty(BmzefWizardData::endDate)
  val enterpriseTitles: ObservableList<String> by property(FXCollections.observableArrayList(enterpriseTitles))
  var contractTitles: ObservableList<String> by property(FXCollections.observableArrayList(contractTitles))
  var kindTitles: ObservableList<String> by property(FXCollections.observableArrayList(kindTitles))
  var activityTitles: ObservableList<String> by property(FXCollections.observableArrayList(activityTitles))
  var entryTexts: ObservableList<String> by property(FXCollections.observableArrayList(entryTexts))
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

  fun reset(enterprises: Set<ActivityPathPart.Enterprise>) {
    projectMapping = ProjectMapping()
    daySummaryModels = emptyList()
    avalailabledEnterprises = enterprises
    beginDate = LocalDate.now().minusDays(1)
    endDate = LocalDate.now()
    Platform.runLater {
      enterpriseTitles.setAll(enterprises.sortedTitles())
    }
    entryTexts.clear()
  }

  val selectedPath: ActivityPath
    get() {
      val enterprise = selectedEnterprise
      @Suppress("SENSELESS_COMPARISON") // "Wir können hier Niemandem trauen"
      return if (null == enterprise)
        ActivityPath.NoPath
      else
        ActivityPath.Path(
          enterprise,
          selectedContract,
          selectedKind,
          selectedActivity
        )
    }
}
