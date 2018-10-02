package wi.co.timetracker.model

import tornadofx.getProperty
import tornadofx.property
import wi.co.timetracker.model.entry.DayModel
import java.io.File
import java.time.LocalDate


class MainModel(
        currentDate: LocalDate = LocalDate.now(),
        file: File? = null,
        fileContent: String = "",
        dayModel: DayModel? = null,
        errors: String = ""
) {

    var currentDate: LocalDate by property(currentDate)
    fun currentDateProperty() = getProperty(MainModel::currentDate)

    var file: File by property(file)

    var fileContent: String by property(fileContent)
    fun fileContentProperty() = getProperty(MainModel::fileContent)

    var dayModel: DayModel? by property(dayModel)

    var errors: String by property(errors)
    fun errorsProperty() = getProperty(MainModel::errors)
}
