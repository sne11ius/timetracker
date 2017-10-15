package wi.co.timetracker.model

import tornadofx.getProperty
import tornadofx.property

class Preferences(baseDir: String = System.getProperty("user.home")) {

    var baseDir by property(baseDir)
    fun baseDirProperty() = getProperty(Preferences::baseDir)

}
