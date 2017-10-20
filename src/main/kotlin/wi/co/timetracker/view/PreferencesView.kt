package wi.co.timetracker.view

import javafx.geometry.Pos
import tornadofx.*
import wi.co.timetracker.controller.PreferencesController

class PreferencesView : View() {

    private val controller: PreferencesController by inject()

    private val preferences = controller.preferences

    override val root = Form()

    init {
        with(root) {
            minWidth = 650.0

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
                field("Reiseindikatoren (getrennt durch Komma)") {
                    textfield().bind(preferences.travelIndicatorsProperty())
                }
                field("Multiplikator Reisezeit") {
                    slider {
                        min = 0.0
                        max = 1.0
                        isShowTickLabels = true
                        isShowTickMarks = true
                        isSnapToTicks = true
                        majorTickUnit = 0.1
                        minorTickCount = 0
                        blockIncrement = 0.1
                    }.bind(preferences.travelMultiplierProperty())
                    label().bind(preferences.travelMultiplierProperty())
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
