package wi.co.timetracker.model

import tornadofx.getProperty
import tornadofx.property

class Preferences(baseDir: String = System.getProperty("user.home"), breakIndicators: String = "", travelIndicators: String = "", travelMultiplier: Number = 1.0f) {

    var baseDir: String by property(baseDir)
    fun baseDirProperty() = getProperty(Preferences::baseDir)

    var breakIndicators: String by property(breakIndicators)
    fun breakIndicatorsProperty() = getProperty(Preferences::breakIndicators)

    var travelIndicators by property(travelIndicators)
    fun travelIndicatorsProperty() = getProperty(Preferences::travelIndicators)

    var travelMultiplier: Number by property(travelMultiplier)
    fun travelMultiplierProperty() = getProperty(Preferences::travelMultiplier)
}
