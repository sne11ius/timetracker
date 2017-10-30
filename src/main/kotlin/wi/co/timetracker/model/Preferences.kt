package wi.co.timetracker.model

import tornadofx.getProperty
import tornadofx.property

class Preferences(
        baseDir: String = System.getProperty("user.home"),
        sapUsername: String = "",
        sapPassword: String = "",
        breakIndicators: String = "",
        travelIndicators: String = "",
        travelMultiplier: Number = 1.0f,
        excelCorrection: Number = 50.0f
) {

    var baseDir: String by property(baseDir)
    fun baseDirProperty() = getProperty(Preferences::baseDir)

    var sapUsername: String by property(sapUsername)
    fun sapUsernameProperty() = getProperty(Preferences::sapUsername)

    var sapPassword: String by property(sapPassword)
    fun sapPasswordProperty() = getProperty(Preferences::sapPassword)

    var breakIndicators: String by property(breakIndicators)
    fun breakIndicatorsProperty() = getProperty(Preferences::breakIndicators)

    var travelIndicators: String by property(travelIndicators)
    fun travelIndicatorsProperty() = getProperty(Preferences::travelIndicators)

    var travelMultiplier: Number by property(travelMultiplier)
    fun travelMultiplierProperty() = getProperty(Preferences::travelMultiplier)

    var excelCorrection: Number by property(excelCorrection)
    fun excelCorrectionProperty() = getProperty(Preferences::excelCorrection)

}
