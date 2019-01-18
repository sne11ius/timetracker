package wi.co.timetracker.view.bmzef

import javafx.scene.layout.Priority.ALWAYS
import tornadofx.*
import wi.co.timetracker.controller.BmzefWizardController

class CheckAssignments: View("Zuordnungen Prüfen") {

  private val model: BmzefWizardData by inject()

  private val controller: BmzefWizardController by inject()

  override val complete = false.toProperty()

  override val root = borderpane {
    top = borderpane {
      left = label("Bitte ordne jeder von dir selbst aufgezeichneten Tätigkeit eine Tätigkeit aus der bmzef zu.\nFalls du einen Eintrag aus dem bmzef vermisst oder diese Funktion zum ersten mal verwendest, klicke auf 'bmzef-Projekte aktualisieren'") {
        padding = tornadofx.insets(5)
      }
      right = button("bmzef-Projekte aktualisieren") {
        action {
          controller.reloadEnterprises()
        }
      }
    }
    val sectionMargin = 10.0
    center = hbox {
      vbox {
        hboxConstraints {
          marginRight = sectionMargin
          hGrow = ALWAYS
        }
        label("Eigener Name")
        entriesListView = listview(model.entryTexts) {
          vboxConstraints {
            vGrow = ALWAYS
          }
        }
        entriesListView!!.bindSelected(model.selectedEntryTextProperty())
        model.selectedEntryTextProperty().onChange { entriesListView!!.selectionModel.select(it) }
      }
      vbox {
        hboxConstraints {
          marginRight = sectionMargin
          hGrow = ALWAYS
        }
        label("Vorhaben")
        enterprisesListView = listview(model.enterpriseTitles) {
          vboxConstraints {
            vGrow = ALWAYS
          }
        }
        enterprisesListView!!.bindSelected(model.selectedEnterpriseProperty())
        model.selectedEnterpriseProperty().onChange { enterprisesListView!!.selectionModel.select(it) }
      }
      vbox {
        hboxConstraints {
          marginRight = sectionMargin
          hGrow = ALWAYS
        }
        label("Vertrag")
        contractsListView = listview(model.contractTitles) {
          vboxConstraints {
            vGrow = ALWAYS
          }
        }
        contractsListView!!.bindSelected(model.selectedContractProperty())
        model.selectedContractProperty().onChange { contractsListView!!.selectionModel.select(it) }
      }
      vbox {
        hboxConstraints {
          marginRight = sectionMargin
          hGrow = ALWAYS
        }
        label("Tätigkeitsart")
        kindsListView = listview(model.kindTitles) {
          vboxConstraints {
            vGrow = ALWAYS
          }
        }
        kindsListView!!.bindSelected(model.selectedKindProperty())
        model.selectedKindProperty().onChange { kindsListView!!.selectionModel.select(it) }
      }
      vbox {
        hboxConstraints {
          hGrow = ALWAYS
        }
        label("Tätigkeit")
        activitiesListView = listview(model.activityTitles) {
          vboxConstraints {
            vGrow = ALWAYS
          }
        }
        activitiesListView!!.bindSelected(model.selectedActivityProperty())
        model.selectedActivityProperty().onChange { activitiesListView!!.selectionModel.select(it) }
      }
    }
  }

}
