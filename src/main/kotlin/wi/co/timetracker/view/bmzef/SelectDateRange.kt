package wi.co.timetracker.view.bmzef

import javafx.beans.binding.Bindings
import jfxtras.scene.control.LocalDatePicker
import tornadofx.View
import tornadofx.booleanBinding
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.onChange
import tornadofx.plusAssign
import wi.co.timetracker.controller.BmzefWizardController

class SelectDateRange : View("Zeitraum wählen") {
  private val model: BmzefWizardData by inject()
  private val controller: BmzefWizardController by inject()

  override val complete = booleanBinding(model.entryTexts) { isNotEmpty() }

  init {
    model.beginDateProperty().onChange {
      controller.reloadEntries()
    }
    model.endDateProperty().onChange {
      controller.reloadEntries()
    }
  }

  override val root = form {
    fieldset(title) {
      field("Start Datum") {
        this += LocalDatePicker().apply {
          prefWidth = 250.0
          allowNull = false
          mode = LocalDatePicker.Mode.SINGLE
          Bindings.bindBidirectional(localDateProperty(), model.beginDateProperty())
        }
      }
      field("End Datum") {
        this += LocalDatePicker().apply {
          prefWidth = 250.0
          allowNull = false
          mode = LocalDatePicker.Mode.SINGLE
          Bindings.bindBidirectional(localDateProperty(), model.endDateProperty())
        }
      }
    }
  }
}
