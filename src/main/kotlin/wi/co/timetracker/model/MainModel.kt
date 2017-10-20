package wi.co.timetracker.model

import tornadofx.getProperty
import tornadofx.property
import java.io.File
import java.time.LocalDate


class MainModel(
        currentDate: LocalDate = LocalDate.now(),
        file: File? = null,
        fileContent: String = "",
        dayModel: DayModel? = null,
        errors: String = ""
) {

    var currentDate by property(currentDate)
    fun currentDateProperty() = getProperty(MainModel::currentDate)

    var file by property(file)
    fun fileProperty() = getProperty(MainModel::file)

    var fileContent by property(fileContent)
    fun fileContentProperty() = getProperty(MainModel::fileContent)

    var dayModel by property(dayModel)
    fun dayModelProperty() = getProperty(MainModel::dayModel)

    var errors by property(errors)
    fun errorsProperty() = getProperty(MainModel::errors)
}
