package wi.co.timetracker.view

import javafx.beans.binding.Bindings
import javafx.geometry.Insets
import javafx.geometry.Pos
import jfxtras.scene.control.LocalDatePicker
import mu.KotlinLogging
import tornadofx.*
import wi.co.timetracker.controller.SapController
import java.time.DayOfWeek
import java.time.LocalDate

class SapFillPreparationView : View() {

    private val logger = KotlinLogging.logger {}

    private val controller: SapController by inject()

    val datePickerBegin = LocalDatePicker().apply {
        prefWidth = 250.0
        allowNull = false
        mode = LocalDatePicker.Mode.SINGLE
        Bindings.bindBidirectional(localDateProperty(), controller.dateBeginProperty())
    }
    val datePickerEnd = LocalDatePicker().apply {
        prefWidth = 250.0
        allowNull = false
        mode = LocalDatePicker.Mode.SINGLE
        Bindings.bindBidirectional(localDateProperty(), controller.dateEndProperty())
    }

    override val root = Form()

    init {
        selectLastWeek()

        datePickerBegin.localDateProperty().addListener({ _, _, new ->
            if (datePickerEnd.localDate.isEqual(new) || datePickerEnd.localDate.isBefore(new)) {
                datePickerEnd.localDate = new.plusDays(1)
            }
        })

        datePickerEnd.localDateProperty().addListener({ _, _, new ->
            if (datePickerBegin.localDate.isEqual(new) || datePickerBegin.localDate.isAfter(new)) {
                datePickerBegin.localDate = new.minusDays(1)
            }
        })

        with(root) {
            fieldset("Achtung") {
                label("Hammerzeit")
            }
            fieldset("Zeitraum auswählen") {
                field("Gewählter Zeitraum") {
                    label(controller.selectedDurationProperty())
                }
                field("Von") {
                    this += datePickerBegin
                }
                field("Bis") {
                    this += datePickerEnd
                }
            }
            hbox {
                button("Letzte Woche") {
                    prefWidth = 370.0
                    action {
                        selectLastWeek()
                    }
                }
            }
            hbox {
                alignment = Pos.BASELINE_RIGHT
                spacing = 10.0
                padding = Insets(10.0)
                button("Abbrechen") {
                    action {
                        close()
                    }
                }
                button("Mach schon!") {
                    action {
                        controller.fillSapGui()
                        close()
                    }
                    isDefaultButton = true
                }
            }
        }
    }

    private fun selectLastWeek() {
        val today = LocalDate.now()
        var end = today
        while (end.dayOfWeek != DayOfWeek.SUNDAY) {
            end = end.plusDays(1)
        }
        if (end == today || end.isAfter(today)) {
            end = end.minusDays(7)
        }
        var begin = end
        while (begin.dayOfWeek != DayOfWeek.MONDAY) {
            begin = begin.minusDays(1)
        }
        datePickerBegin.localDate = begin
        datePickerEnd.localDate = end
    }

}
