package wi.co.timetracker.sap

import javafx.scene.control.Alert
import mu.KotlinLogging
import org.sikuli.script.ImagePath
import org.sikuli.script.Screen
import org.springframework.core.io.ClassPathResource

object SapControl {

    private val log = KotlinLogging.logger {}
    private val s = Screen()

    init {
        ImagePath.add(ClassPathResource("images").url)
        s.autoWaitTimeout = 20.0
    }

    fun doStuff(sapUsername: String, sapPassword: String) {
        if (sapUsername.isBlank()) {
            Alert(Alert.AlertType.ERROR, "Bitte gültigen SAP-Benutzernamen angeben.").show()
            return
        }
        if (sapPassword.isBlank()) {
            Alert(Alert.AlertType.ERROR, "Bitte gültiges SAP-Kennwort angeben.").show()
            return
        }
        launchSap(sapUsername, sapPassword)
    }

    fun launchSap(sapUsername: String, sapPassword: String) {
        with(s) {
            autoWaitTimeout
            click("windows_start_button.png")
            wait(1.0)
            type("sap")
            click("sap_gui_logo.png")
            click("login_button.png")
            click("username_field.png")
            type(sapUsername)
            click("password_field.png")
            type(sapPassword + "\n")
            doubleClick("tree_zeiterfassung.png")
            doubleClick("tree_zeiten_pflegen.png")
        }
    }

}
