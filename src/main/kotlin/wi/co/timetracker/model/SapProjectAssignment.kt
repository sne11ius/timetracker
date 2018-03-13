package wi.co.timetracker.model

import javafx.beans.property.SimpleStringProperty
import tornadofx.property

class SapProjectAssignment(userProjectName: String, sapProjectName: String) {

    val userProjectNameProperty = SimpleStringProperty(userProjectName)
    private var userProjectName: String by property(userProjectName)

    val sapProjectNameProperty = SimpleStringProperty(sapProjectName)
    private var sapProjectName: String by property(sapProjectName)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SapProjectAssignment

        if (userProjectName != other.userProjectName) return false
        if (sapProjectName != other.sapProjectName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userProjectName.hashCode()
        result = 31 * result + sapProjectName.hashCode()
        return result
    }

    override fun toString(): String {
        return "SapProjectAssignment(userProjectNameProperty=$userProjectNameProperty, sapProjectNameProperty=$sapProjectNameProperty)"
    }

}
