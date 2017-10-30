package wi.co.timetracker.sap

import javafx.scene.control.Alert
import mu.KotlinLogging
import org.sikuli.script.*
import org.springframework.core.io.ClassPathResource
import wi.co.timetracker.extensions.formatDefault
import wi.co.timetracker.model.DaySummaryModel
import java.time.LocalDate

object SapControl {

    private val log = KotlinLogging.logger {}
    private val s = Screen()

    init {
        ImagePath.add(ClassPathResource("images").url)
        s.autoWaitTimeout = 20.0
    }

    fun findAllProjectNames(sapUsername: String, sapPassword: String, day: LocalDate): List<String> {
        if (!isSapRunning()) {
            launchSap()
        }
        if (!isLoggedIn()) {
            goToTop()
        }
        doLogin(sapUsername, sapPassword)
        openTimetracking(day)
        val projectNames = mutableListOf<String>()
        with(s) {
            val begin = find("arbeitsvorrat.png").right(169).below(60)
            while (true) {
                mouseMove(begin)
                mouseDown(Button.LEFT)
                mouseMove(begin.right(430))
                mouseUp()
                type("c", Key.CTRL)
                val projectName = App.getClipboard()
                log.debug { "Found project $projectName" }
                projectNames += projectName
                try {
                    wait("projects_scrolled_down_indicator.png", 1.0)
                    return projectNames
                } catch (e: FindFailed) {
                    find("scroll_projects_down_indicator.png").left(-10).above(-15).click()
                    wait(3.0)
                }
            }
        }
        return projectNames
    }

    /*
    fun doStuff(sapUsername: String, sapPassword: String, summaries: List<DaySummaryModel>) {
        if (sapUsername.isBlank()) {
            Alert(Alert.AlertType.ERROR, "Bitte gültigen SAP-Benutzernamen angeben.").show()
            return
        }
        if (sapPassword.isBlank()) {
            Alert(Alert.AlertType.ERROR, "Bitte gültiges SAP-Kennwort angeben.").show()
            return
        }
        //launchSap(sapUsername, sapPassword)
        //openTimetracking()
    }
    */

    private fun isSapRunning(): Boolean {
        with(s) {
            return try {
                wait("sap_running_indicator.png", 2.0)
                true
            } catch (e: FindFailed) {
                false
            }
        }
    }

    private fun isNotLoggedIn(): Boolean {
        with(s) {
            return try {
                wait("sap_login_indicator.png", 2.0)
                true
            } catch (e: FindFailed) {
                false
            }
        }
    }

    private fun isLoggedIn(): Boolean = !isNotLoggedIn()

    private fun openTimetracking(day: LocalDate) {
        with(s) {
            doubleClick("tree_zeiterfassung.png")
            doubleClick("tree_zeiten_pflegen.png")
            click(find("sap_start_date.png").right(53))
            type("a", Key.CTRL)
            type(day.formatDefault())
            click("edit_button.png")
        }
    }

    private fun launchSap() {
        with(s) {
            click("windows_start_button.png")
            wait(1.0)
            type("sap")
            click("sap_gui_logo.png")
        }
    }

    private fun doLogin(sapUsername: String, sapPassword: String) {
        with(s) {
            click("login_button.png")
            click("username_field.png")
            type(sapUsername)
            click("password_field.png")
            type(sapPassword + "\n")
        }
    }

    private fun goToTop() {
        with(s) {
            click("sap_close_button.png")
            click("sap_ja_button.png")
        }
    }

}
