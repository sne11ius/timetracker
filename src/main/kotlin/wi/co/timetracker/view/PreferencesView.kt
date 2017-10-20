package wi.co.timetracker.view

import javafx.geometry.Pos
import javafx.scene.control.TextField
import tornadofx.*
import wi.co.timetracker.controller.PreferencesController

class PreferencesView : View() {

    private val controller: PreferencesController by inject()

    private val preferences = controller.preferences

    var showBaseDirectoryField: TextField by singleAssign()

    override val root = Form()

    init {
        with(root) {
            minWidth = 500.0

            fieldset("Einstellungen") {
                field("Basisverzeichnis") {
                    textfield() {
                        isEditable = false
                        isDisable = true
                    }.bind(preferences.baseDirProperty())
                    button("Ändern") {
                        action {
                            val newDir = chooseDirectory("Basisverzeichnis wählen")
                            if (null != newDir) {
                                preferences.baseDir = newDir.absolutePath
                            }
                        }
                    }
                }
                field("Pausenindikatoren (getrennt durch Komma)") {
                    textfield().bind(preferences.breakIndicatorsProperty())
                }
            }
            hbox {
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

}
