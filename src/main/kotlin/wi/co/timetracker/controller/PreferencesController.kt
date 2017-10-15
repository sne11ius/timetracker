package wi.co.timetracker.controller

import tornadofx.Controller
import wi.co.timetracker.model.Preferences


class PreferencesController : Controller() {

    val preferences = Preferences()

    init {
        resetPreferences()
    }

    private fun resetPreferences() {
        preferences(PREFS_NAME) {
            preferences.baseDir = get(BASE_DIR, System.getProperty("user.home"))
        }
    }

    fun discard() {
        resetPreferences()
    }

    fun save() {
        val baseDir = preferences.baseDir
        println("Saving baseDir: $baseDir")
        preferences(PREFS_NAME) {
            put(BASE_DIR, baseDir)
        }
    }

    companion object {
        val PREFS_NAME = "timetracker"
        val BASE_DIR = "baseDir"
    }
}
