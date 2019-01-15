package wi.co.timetracker.view.bmzef

import javafx.beans.property.Property
import tornadofx.*


class WaitController(
  progress: Double = 0.0
): Controller() {
  private var progress: Number by property(progress)
  fun progressProperty(): Property<Number> = getProperty(WaitController::progress)
}
