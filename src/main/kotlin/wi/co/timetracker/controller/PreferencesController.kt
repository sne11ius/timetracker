package wi.co.timetracker.controller

import tornadofx.Controller
import wi.co.timetracker.extensions.toFile
import wi.co.timetracker.model.Preferences
import java.io.File
import java.time.Duration


class PreferencesController : Controller() {

    interface OnPreferencesUpdatedListener {
        fun onPreferencesUpdated()
    }

    val preferences = Preferences()

    private var prefsUpdateListener: OnPreferencesUpdatedListener? = null

    init {
        resetPreferences()
    }

    fun discard() {
        resetPreferences()
    }

    fun save() {
        preferences(PREFS_NAME) {
            put(BASE_DIR, preferences.baseDir)
            put(BREAK_INDICATORS, preferences.breakIndicators)
            put(TRAVEL_INDICATORS, preferences.travelIndicators)
            put(TRAVEL_MULTIPLIER, preferences.travelMultiplier.toString())
            put(EXCEL_CORRECTION, preferences.excelCorrection.toString())
        }
        prefsUpdateListener?.onPreferencesUpdated()
    }

    fun setOnPreferencesUpdatedListener(listener: OnPreferencesUpdatedListener?) {
        prefsUpdateListener = listener
    }

    fun getBreakIndicators(): List<String> = splitItems(preferences.breakIndicators)

    fun getTravelIndicators(): List<String> = splitItems(preferences.travelIndicators)

    fun getTravelMultiplier(): Float {
        return preferences.travelMultiplier.toFloat()
    }

    fun getExcelCorrection(): Duration {
        return Duration.ofMinutes(preferences.excelCorrection.toFloat().toLong())
    }

    fun getBaseDir(): File = preferences.baseDir.toFile()

    private fun splitItems(pref: String): List<String> {
        return pref.split(",").map { it.trim() }.filter { it.isNotBlank() }
    }

    private fun resetPreferences() {
        preferences(PREFS_NAME) {
            preferences.baseDir = get(BASE_DIR, System.getProperty("user.home"))
            preferences.breakIndicators = get(BREAK_INDICATORS, "")
            preferences.travelIndicators = get(TRAVEL_INDICATORS, "")
            preferences.travelMultiplier = get(TRAVEL_MULTIPLIER, "1.0").toFloat()
            preferences.excelCorrection = get(EXCEL_CORRECTION, "30.0").toFloat()
        }
    }

    companion object {
        val PREFS_NAME = "timetracker"
        val BASE_DIR = "baseDir"
        val BREAK_INDICATORS = "breakIndicators"
        val TRAVEL_INDICATORS = "travelIndicators"
        val TRAVEL_MULTIPLIER = "travelMultiplier"
        val EXCEL_CORRECTION = "excelCorrection"
    }

}
