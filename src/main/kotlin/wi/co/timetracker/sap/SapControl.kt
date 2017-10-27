package wi.co.timetracker.sap

import javafx.scene.control.Alert
import mmarquee.automation.UIAutomation
import mmarquee.automation.controls.AutomationBase
import mmarquee.automation.controls.AutomationWindow
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import wi.co.timetracker.controller.PreferencesController

object SapControl {

    private val log = KotlinLogging.logger {}

    fun doStuff(sapExecutablePath: String, sapUsername: String, sapPassword: String) {
        if (sapExecutablePath.isBlank()) {
            Alert(Alert.AlertType.ERROR, "Bitte erst den Pfad zu sapgui.exe konfigurieren").show()
            return
        }
        if (sapUsername.isBlank()) {
            Alert(Alert.AlertType.ERROR, "Bitte gültigen SAP-Benutzernamen angeben.").show()
            return
        }
        if (sapPassword.isBlank()) {
            Alert(Alert.AlertType.ERROR, "Bitte gültiges SAP-Kennwort angeben.").show()
            return
        }
        //val automation = UIAutomation.getInstance()
        //val sap = automation.launchOrAttach(sapExecutablePath)
        //var loginWindow: AutomationWindow? = null
        //try {
        //    for (i in 1..3) {
        //        loginWindow = automation.getDesktopWindow("SAP Logon 730")
        //        break
        //    }
        //} catch (e: Exception) {
        //    Alert(Alert.AlertType.ERROR, "SAP konnte nicht gestartet werden.").show()
        //    return
        //}
        //val wnd = loginWindow!!
        //wnd.getButton("Anmelden").click()

        //val mainWindow = automation.getDesktopWindow("SAP")
        //mainWindow.getChildren(true).forEach({
        //    el:AutomationBase -> try{println(el.getName())}
        // catch (e:Exception){}})
        //loginWindow.
        //loginWindow.getTextBoxByAutomationId("01000080B8020E00FCFFFFFF00000000").element.setFocus()

        // println("hehe")
    }

}
