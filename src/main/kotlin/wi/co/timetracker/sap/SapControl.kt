package wi.co.timetracker.sap

import javafx.scene.control.Alert
import mu.KotlinLogging
import org.sikuli.script.ImagePath
import org.sikuli.script.Screen
import org.springframework.core.io.ClassPathResource

object SapControl {

    private val log = KotlinLogging.logger {}

    fun doStuff(sapExecutablePath: String, sapUsername: String, sapPassword: String) {
        if (sapUsername.isBlank()) {
            Alert(Alert.AlertType.ERROR, "Bitte gültigen SAP-Benutzernamen angeben.").show()
            return
        }
        if (sapPassword.isBlank()) {
            Alert(Alert.AlertType.ERROR, "Bitte gültiges SAP-Kennwort angeben.").show()
            return
        }
        if (null == SapControl::class.java.getResourceAsStream("/images/1509091819886.png")) {
            log.error { "No image" }
        } else {
            ImagePath.add(ClassPathResource("images").url)
            val s = Screen()
            s.click("1509091819886.png")
        }
    }

}
