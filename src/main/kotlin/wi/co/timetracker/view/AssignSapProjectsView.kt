package wi.co.timetracker.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import tornadofx.*
import wi.co.timetracker.controller.AssignSapProjectsController
import wi.co.timetracker.model.SapProjectAssignment

class AssignSapProjectsView : View() {

    private val controller: AssignSapProjectsController by inject()

    override val root = borderpane {
        minWidth = 400.0
        padding = Insets(10.0)
        center = tableview(controller.assignments) {
            column("Projektname", SapProjectAssignment::userProjectNameProperty)
            column("SAP-Bezeichnung", SapProjectAssignment::sapProjectNameProperty)
                    .makeEditable()
                    .useComboBox(controller.availableSapProjects, { _ ->
                        controller.updateMappingComplete()
                    })
            columnResizePolicy = SmartResize.POLICY
        }
        bottom = hbox {
            alignment = Pos.BASELINE_RIGHT
            spacing = 10.0
            padding = Insets(10.0)
            button("Abbrechen") {
                action {
                    controller.discard()
                    close()
                }
            }
            button("Speichern") {
                disableProperty().bind(controller.saveDisabledProperty)
                action {
                    controller.updateMappingComplete()
                    close()
                }
                isDefaultButton = true
            }
        }
    }

}
