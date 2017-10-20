package wi.co.timetracker.controller

import tornadofx.Controller
import wi.co.timetracker.model.Preferences


class PreferencesController : Controller() {

    val preferences = Preferences()

    interface OnPreferencesUpdatedListener {
        fun onPreferencesUpdated()
    }

    init {
        resetPreferences()
    }

    private var prefsUpdateListener: OnPreferencesUpdatedListener? = null

    fun setOnPreferencesUpdatedListener(listener: OnPreferencesUpdatedListener?) {
        prefsUpdateListener = listener
    }

    fun getBreakIndicators(): List<String> {
        return preferences.breakIndicators.split(",").map { s ->
            s.trim()
        }
    }

    private fun resetPreferences() {
        preferences(PREFS_NAME) {
            preferences.baseDir = get(BASE_DIR, System.getProperty("user.home"))
            preferences.breakIndicators = get(BREAK_INDICATORS, "")
        }
    }

    fun discard() {
        resetPreferences()
    }

    fun save() {
        preferences(PREFS_NAME) {
            put(BASE_DIR, preferences.baseDir)
            put(BREAK_INDICATORS, preferences.breakIndicators)
        }
        prefsUpdateListener?.onPreferencesUpdated()
    }

    companion object {
        val PREFS_NAME = "timetracker"
        val BASE_DIR = "baseDir"
        val BREAK_INDICATORS = "breakIndicators"
    }
}
