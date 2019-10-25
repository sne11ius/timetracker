package wi.co.timetracker.controller

import java.io.File
import java.time.Duration
import tornadofx.Controller
import wi.co.timetracker.extensions.toFile
import wi.co.timetracker.model.Preferences

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
      put(BMZEF_USERNAME, preferences.bmzefUsername)
      put(BMZEF_PASSWORD, preferences.bmzefPassword)
      put(BMZEF_URL, preferences.bmzefUrl)
      put(BMZEF_IGNORE_INDICATORS, preferences.bmzefIgnoreIndicators)
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

  val breakIndicators: List<String> = splitItems(preferences.breakIndicators)

  val travelIndicators: List<String> = splitItems(preferences.travelIndicators)

  val travelMultiplier: Float = preferences.travelMultiplier.toFloat()

  val excelCorrection: Duration = Duration.ofMinutes(preferences.excelCorrection.toFloat().toLong())

  val baseDir: File = preferences.baseDir.toFile()

  val bmzefUsername: String = preferences.bmzefUsername

  val bmzefPassword: String = preferences.bmzefPassword

  val bmzefBaseUrl: String = preferences.bmzefUrl.removeSuffix("/")

  val bmzefIgnoreIndicators: List<String> = splitItems(preferences.bmzefIgnoreIndicators)

  private fun splitItems(pref: String): List<String> {
    return pref.split(",").map { it.trim() }.filter { it.isNotBlank() }
  }

  private fun resetPreferences() {
    preferences(PREFS_NAME) {
      preferences.baseDir = get(BASE_DIR, System.getProperty("user.home"))
      preferences.bmzefUsername = get(BMZEF_USERNAME, "")
      preferences.bmzefPassword = get(BMZEF_PASSWORD, "")
      preferences.bmzefUrl = get(BMZEF_URL, "")
      preferences.bmzefIgnoreIndicators = get(BMZEF_IGNORE_INDICATORS, "")
      preferences.breakIndicators = get(BREAK_INDICATORS, "")
      preferences.travelIndicators = get(TRAVEL_INDICATORS, "")
      preferences.travelMultiplier = get(TRAVEL_MULTIPLIER, "1.0").toFloat()
      preferences.excelCorrection = get(EXCEL_CORRECTION, "50.0").toFloat()
    }
  }

  companion object {
    const val PREFS_NAME = "timetracker"
    const val BASE_DIR = "baseDir"
    const val BMZEF_USERNAME = "bmzefUsername"
    const val BMZEF_PASSWORD = "bmzefPassword"
    const val BMZEF_URL = "bmzefUrl"
    const val BMZEF_IGNORE_INDICATORS = "bmzefIgnoreIndicators"
    const val BREAK_INDICATORS = "breakIndicators"
    const val TRAVEL_INDICATORS = "travelIndicators"
    const val TRAVEL_MULTIPLIER = "travelMultiplier"
    const val EXCEL_CORRECTION = "excelCorrection"
  }
}
