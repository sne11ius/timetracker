package wi.co.timetracker.view.bmzef

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.*
import wi.co.timetracker.service.mbzef.BmzefService
import java.time.LocalDate

class BmzefWizardData(
  projectMapping: BmzefService.ProjectMapping = BmzefService.ProjectMapping(),
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
): ViewModel() {
  var projectMapping: BmzefService.ProjectMapping by property(projectMapping)

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
