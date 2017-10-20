package wi.co.timetracker.view

import javafx.beans.binding.Bindings.bindBidirectional
import javafx.geometry.Insets
import javafx.geometry.NodeOrientation
import javafx.geometry.Orientation
import jfxtras.scene.control.CalendarPicker
import tornadofx.*
import wi.co.timetracker.controller.MainController

class MainView : View() {

    private val controller: MainController by inject()

    override val root = borderpane {
        minWidth = 500.0
        minHeight = 500.0
        padding = Insets(10.0)
        top = buttonbar {
            button("Preferences") {
                action {
                    find(PreferencesView::class).openModal()
                }
            }
        }
        center = borderpane {
            top = hbox {
                label() { prefWidth = 100.0 }.bind(controller.dayPartProperty())
                label() { prefWidth = 100.0 }.bind(controller.weekPartProperty())
                label() { prefWidth = 100.0 }.bind(controller.monthPartProperty())
            }
            center = splitpane(Orientation.VERTICAL) {
                borderpane {
                    left = textarea {
                        prefWidth = 40.0
                        minWidth = 40.0
                        isEditable = false
                        isDisable = true
                        bind(controller.lineNumbersProperty())
                        nodeOrientation = NodeOrientation.RIGHT_TO_LEFT
                    }
                    center = textarea {
                        bind(controller.mainModel.fileContentProperty())
                    }
                }
                textarea {
                    isEditable = false
                }.bind(controller.mainModel.errorsProperty())
            }
        }
        right = vbox {
            spacing = 10.0
            padding = Insets(10.0)
            this += CalendarPicker().apply {
                prefWidth = 250.0
                prefHeight = 250.0
                allowNull = false
                mode = CalendarPicker.Mode.SINGLE
                bindBidirectional(calendarProperty(), controller.mainModel.currentDateProperty())
            }
        }
    }
}
