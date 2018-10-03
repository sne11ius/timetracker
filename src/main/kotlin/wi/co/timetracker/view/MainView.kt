package wi.co.timetracker.view

import javafx.beans.binding.Bindings.bindBidirectional
import javafx.geometry.Insets
import javafx.geometry.NodeOrientation
import javafx.geometry.Orientation
import javafx.scene.control.SelectionMode
import jfxtras.scene.control.LocalDatePicker
import mu.KotlinLogging
import tornadofx.*
import wi.co.timetracker.controller.MainController
import java.time.LocalDate

class MainView : View() {

  private val logger = KotlinLogging.logger {}

  private val controller: MainController by inject()

  override val root = borderpane {
    minWidth = 500.0
    minHeight = 500.0
    paddingAll = 10.0
    top = buttonbar {
      button("Zeit it!") {
        action {
          logger.debug { "Zeiterfassing now" }
          controller.runTimeTracking()
        }
      }
      button("Einstellungen") {
        action {
          find(PreferencesView::class).openModal()
        }
      }
    }
    center = borderpane {
      top = hbox {
        label { prefWidth = 160.0 }.bind(controller.dayPartProperty())
        label { prefWidth = 160.0 }.bind(controller.weekPartProperty())
        label { prefWidth = 160.0 }.bind(controller.monthPartProperty())
      }
      center = tabpane {
        tab("Daten eingeben") {
          isClosable = false
          splitpane {
            splitpane(Orientation.VERTICAL) {
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
            textarea {
              prefWidth = 300.0
              minWidth = 300.0
              isEditable = false
              bind(controller.summaryProperty())
            }
          }
        }
        tab("FI-Summary") {
          isClosable = false
          borderpane {
            top = label("Summe (ohne Excel-Korrektur): 50") {
              paddingAll = 10.0
              bind(controller.excelSummarySumProperty())
            }
            center = borderpane {
              center = hbox {
                prefWidth = 500.0
                borderpane {
                  paddingAll = 10.0
                  top = label("Tag") {
                    paddingAll = 10.0
                  }
                  center = textarea {
                    isEditable = false
                    bind(controller.excelSummaryDateProperty())
                  }
                }
                borderpane {
                  paddingAll = 10.0
                  top = label("Uhrzeit") {
                    paddingAll = 10.0
                  }
                  center = textarea {
                    isEditable = false
                    bind(controller.excelSummaryTimeProperty())
                  }
                }
                borderpane {
                  paddingAll = 10.0
                  top = label("Beschreibung") {
                    paddingAll = 10.0
                  }
                  center = textarea {
                    isEditable = false
                    bind(controller.excelSummaryDescriptionProperty())
                  }
                }
              }
            }
            right = listview(controller.projectsInMonth) {
              selectionModel.selectionMode = SelectionMode.SINGLE
              bindSelected(controller.currentExcelSummaryProjectProperty())
            }
          }
        }
        tab("Charts") {
          isClosable = false
          piechart("Monat", controller.monthChartData)
        }
        tab("Fehler") {
          isClosable = false
          splitpane {
            splitpane(Orientation.VERTICAL) {
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
            borderpane {
              center = listview(controller.daysWithErrors) {
                selectionModel.selectionMode = SelectionMode.SINGLE
                bindSelected(controller.mainModel.currentDateProperty())
              }
              bottom = buttonbar {
                padding = Insets(10.0, 0.0, 0.0, 0.0)
                button("Fehler suchen") {
                  action {
                    controller.readDatesWithErrors()
                  }
                }
                button("Auto fix") {
                  action {
                    controller.autoFixFiles()
                  }
                }
              }
            }
          }
        }
      }
    }
    right = vbox {
      spacing = 10.0
      paddingAll = 10.0
      this += LocalDatePicker().apply {
        prefWidth = 250.0
        allowNull = false
        mode = LocalDatePicker.Mode.SINGLE
        bindBidirectional(localDateProperty(), controller.mainModel.currentDateProperty())
      }
      button("Heute") {
        prefWidth = 250.0
        action {
          controller.mainModel.currentDate = LocalDate.now()
        }
      }
    }
  }
}
