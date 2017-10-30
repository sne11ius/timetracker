package wi.co.timetracker.view

import javafx.geometry.Pos
import tornadofx.*
import wi.co.timetracker.controller.AssignSapProjectsController
import wi.co.timetracker.model.SapProjectAssignment

class AssignSapProjectsView : View() {

    private val controller: AssignSapProjectsController by inject()

    override val root = borderpane {
        center = tableview(controller.assignments) {
            column("Projektname", SapProjectAssignment::userProjectNameProperty)
            column("SAP-Bezeichnung", SapProjectAssignment::sapProjectNameProperty).makeEditable().useComboBox(controller.availableSapProjects)
            columnResizePolicy = SmartResize.POLICY
        }
        bottom = hbox {
            alignment = Pos.BASELINE_RIGHT
            spacing = 10.0
            button("Abbrechen") {
                action {
                    controller.discard()
                    close()
                }
            }
            button("Speichern") {
                action {
                    controller.save()
                    close()
                }
                isDefaultButton = true
            }
        }
    }

}