package wi.co.timetracker.model

import tornadofx.getProperty
import tornadofx.property

class Preferences(baseDir: String = System.getProperty("user.home"), breakIndicators: String = "") {

    var baseDir by property(baseDir)
    fun baseDirProperty() = getProperty(Preferences::baseDir)

    var breakIndicators by property(breakIndicators)
    fun breakIndicatorsProperty() = getProperty(Preferences::breakIndicators)

}
