package wi.co.timetracker.view

import javafx.beans.binding.Bindings.bindBidirectional
import javafx.geometry.Insets
import javafx.geometry.NodeOrientation
import javafx.geometry.Orientation
import javafx.scene.control.SelectionMode
import jfxtras.scene.control.LocalDatePicker
import mu.KotlinLogging
import tornadofx.*
import wi.co.timetracker.controller.BmzefWizardController
import wi.co.timetracker.controller.MainController
import java.time.LocalDate

class MainView : View() {

  private val logger = KotlinLogging.logger {}

  private val mainController: MainController by inject()
  private val bmzefWizardController: BmzefWizardController by inject()

  override val root = borderpane {
    minWidth = 500.0
    minHeight = 500.0
    paddingAll = 10.0
    top = buttonbar {
      button("bmzef it!") {
        action {
          logger.debug { "Zeiterfassing now" }
          bmzefWizardController.runTimeTracking()
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
        label { prefWidth = 160.0 }.bind(mainController.dayPartProperty())
        label { prefWidth = 160.0 }.bind(mainController.weekPartProperty())
        label { prefWidth = 160.0 }.bind(mainController.monthPartProperty())
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
                  bind(mainController.lineNumbersProperty())
                  nodeOrientation = NodeOrientation.RIGHT_TO_LEFT
                }
                center = textarea {
                  bind(mainController.mainModel.fileContentProperty())
                }
              }
              textarea {
                isEditable = false
              }.bind(mainController.mainModel.errorsProperty())
            }
            textarea {
              prefWidth = 300.0
              minWidth = 300.0
              isEditable = false
              bind(mainController.summaryProperty())
            }
          }
        }
        tab("FI-Summary") {
          isClosable = false
          borderpane {
            top = label("Summe (ohne Excel-Korrektur): 50") {
              paddingAll = 10.0
              bind(mainController.excelSummarySumProperty())
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
                    bind(mainController.excelSummaryDateProperty())
                  }
                }
                borderpane {
                  paddingAll = 10.0
                  top = label("Uhrzeit") {
                    paddingAll = 10.0
                  }
                  center = textarea {
                    isEditable = false
                    bind(mainController.excelSummaryTimeProperty())
                  }
                }
                borderpane {
                  paddingAll = 10.0
                  top = label("Beschreibung") {
                    paddingAll = 10.0
                  }
                  center = textarea {
                    isEditable = false
                    bind(mainController.excelSummaryDescriptionProperty())
                  }
                }
              }
            }
            right = listview(mainController.projectsInMonth) {
              selectionModel.selectionMode = SelectionMode.SINGLE
              bindSelected(mainController.currentExcelSummaryProjectProperty())
            }
          }
        }
        tab("Charts") {
          isClosable = false
          piechart("Monat", mainController.monthChartData)
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
                  bind(mainController.lineNumbersProperty())
                  nodeOrientation = NodeOrientation.RIGHT_TO_LEFT
                }
                center = textarea {
                  bind(mainController.mainModel.fileContentProperty())
                }
              }
              textarea {
                isEditable = false
              }.bind(mainController.mainModel.errorsProperty())
            }
            borderpane {
              center = listview(mainController.daysWithErrors) {
                selectionModel.selectionMode = SelectionMode.SINGLE
                bindSelected(mainController.mainModel.currentDateProperty())
              }
              bottom = buttonbar {
                padding = Insets(10.0, 0.0, 0.0, 0.0)
                button("Fehler suchen") {
                  action {
                    mainController.readDatesWithErrors()
                  }
                }
                button("Auto fix") {
                  action {
                    mainController.autoFixFiles()
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
        bindBidirectional(localDateProperty(), mainController.mainModel.currentDateProperty())
      }
      hbox {
        button("\uD83E\uDC50") {
          prefWidth = 125.0
          action {
            mainController.mainModel.currentDate = mainController.mainModel.currentDate.minusDays(1)
          }
        }
        button("\uD83E\uDC52") {
          prefWidth = 125.0
          action {
            mainController.mainModel.currentDate = mainController.mainModel.currentDate.plusDays(1)
          }
        }
      }
      button("Heute") {
        prefWidth = 250.0
        action {
          mainController.mainModel.currentDate = LocalDate.now()
        }
      }
    }
  }
}
