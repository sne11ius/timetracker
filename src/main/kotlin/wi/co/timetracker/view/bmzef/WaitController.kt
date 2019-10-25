package wi.co.timetracker.view.bmzef

import javafx.beans.property.Property
import tornadofx.Controller
import tornadofx.getProperty
import tornadofx.property

class WaitController(
  progress: Double = 0.0
) : Controller() {
  private var progress: Number by property(progress)
  fun progressProperty(): Property<Number> = getProperty(WaitController::progress)
}
