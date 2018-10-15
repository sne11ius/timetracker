package wi.co.timetracker.view.bmzef

import mu.KotlinLogging
import tornadofx.*

class CheckAssignments: View("Zuordnungen Prüfen") {

  private val model: BmzefWizardData by inject()

  override val root = borderpane {
    prefWidth = 800.0
    top = borderpane {
      left = label("Bitte ordne jeder von dir selbst aufgezeichneten Tätigkeit eine Tätigkeit aus der bmzef zu.\nFalls du einen Eintrag aus dem bmzef vermisst oder diese Funktion zum ersten mal verwendest, klicke auf 'bmzef-Projekte aktualisieren'") {
        padding = tornadofx.insets(5)
      }
      right = button("bmzef-Projekte aktualisieren")
    }
    center = hbox {
      vbox {
        label("Eigener Name")
        entriesListView = listview(model.entryTexts)
        entriesListView!!.bindSelected(model.selectedEntryTextProperty())
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
