package wi.co.timetracker.view.bmzef

import javafx.geometry.Pos.CENTER
import tornadofx.View
import tornadofx.label
import tornadofx.paddingAll
import tornadofx.progressindicator
import tornadofx.vbox

class WaitDialog : View("Warten...") {

  private val waitController: WaitController by inject()

  override val root = vbox {
    paddingAll = 10
    label("Daten werden geladen...") {
      minWidth = 400.0
      alignment = CENTER
      paddingAll = 10
    }
    progressindicator(waitController.progressProperty())
  }
}
