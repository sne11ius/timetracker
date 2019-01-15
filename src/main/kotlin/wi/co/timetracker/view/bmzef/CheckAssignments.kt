package wi.co.timetracker.view.bmzef

import javafx.beans.binding.BooleanExpression
import tornadofx.*

class CheckAssignments: View("Zuordnungen Prüfen") {

  private val model: BmzefWizardData by inject()

  override val complete: BooleanExpression
    get() = model.projectMapping.isComplete.toProperty()

  override val root = borderpane {
    prefWidth = 800.0
    top = borderpane {
      left = label("Bitte ordne jeder von dir selbst aufgezeichneten Tätigkeit eine Tätigkeit aus der bmzef zu.\nFalls du einen Eintrag aus dem bmzef vermisst oder diese Funktion zum ersten mal verwendest, klicke auf 'bmzef-Projekte aktualisieren'") {
        padding = tornadofx.insets(5)
      }
    }
    center = hbox {
      vbox {
        label("Eigener Name")
        entriesListView = listview(model.entryTexts)
        entriesListView!!.bindSelected(model.selectedEntryTextProperty())
        model.selectedEntryTextProperty().onChange { entriesListView!!.selectionModel.select(it) }
      }
      vbox {
        label("Vorhaben")
        enterprisesListView = listview(model.enterpriseTitles)
        enterprisesListView!!.bindSelected(model.selectedEnterpriseProperty())
        model.selectedEnterpriseProperty().onChange { enterprisesListView!!.selectionModel.select(it) }
      }
      vbox {
        label("Vertrag")
        contractsListView = listview(model.contractTitles)
        contractsListView!!.bindSelected(model.selectedContractProperty())
        model.selectedContractProperty().onChange { contractsListView!!.selectionModel.select(it) }
      }
      vbox {
        label("Tätigkeitsart")
        kindsListView = listview(model.kindTitles)
        kindsListView!!.bindSelected(model.selectedKindProperty())
        model.selectedKindProperty().onChange { kindsListView!!.selectionModel.select(it) }
      }
      vbox {
        label("Tätigkeit")
        activitiesListView = listview(model.activityTitles)
        activitiesListView!!.bindSelected(model.selectedActivityProperty())
        model.selectedActivityProperty().onChange { activitiesListView!!.selectionModel.select(it) }
      }
    }
  }

}
