package wi.co.timetracker.model

import tornadofx.getProperty
import tornadofx.property

class Preferences(
  baseDir: String = System.getProperty("user.home"),
  bmzefUsername: String = "",
  bmzefPassword: String = "",
  bmzefUrl: String = "",
  bmzefIgnoreIndicators: String = "",
  breakIndicators: String = "",
  travelIndicators: String = "",
  travelMultiplier: Number = 1.0f,
  excelCorrection: Number = 50.0f
) {

  var baseDir: String by property(baseDir)
  fun baseDirProperty() = getProperty(Preferences::baseDir)

  var bmzefUsername: String by property(bmzefUsername)
  fun bmzefUsernameProperty() = getProperty(Preferences::bmzefUsername)

  var bmzefPassword: String by property(bmzefPassword)
  fun bmzefPasswordProperty() = getProperty(Preferences::bmzefPassword)

  var bmzefUrl: String by property(bmzefUrl)
  fun bmzefUrlProperty() = getProperty(Preferences::bmzefUrl)

  var bmzefIgnoreIndicators: String by property(bmzefIgnoreIndicators)
  fun bmzefIgnoreIndicatorsProperty() = getProperty(Preferences::bmzefIgnoreIndicators)

    var breakIndicators: String by property(breakIndicators)
  fun breakIndicatorsProperty() = getProperty(Preferences::breakIndicators)

  var travelIndicators: String by property(travelIndicators)
  fun travelIndicatorsProperty() = getProperty(Preferences::travelIndicators)

  var travelMultiplier: Number by property(travelMultiplier)
  fun travelMultiplierProperty() = getProperty(Preferences::travelMultiplier)

  var excelCorrection: Number by property(excelCorrection)
  fun excelCorrectionProperty() = getProperty(Preferences::excelCorrection)
}
