package wi.co.timetracker.service.mbzef

import tornadofx.*
import wi.co.timetracker.controller.PreferencesController
import wi.co.timetracker.model.bmzef.Activity
import wi.co.timetracker.model.bmzef.Contract
import wi.co.timetracker.model.bmzef.Enterprise
import wi.co.timetracker.model.bmzef.Kind

class BmzefService: Controller() {

  private val preferencesController: PreferencesController by inject()

  private val driverProvider: WebDriverProvider by inject()

  fun readAvailableEnterprises(): List<Enterprise> {
    val driver = driverProvider.createDriver()
    val baseUrl = preferencesController.bmzefBaseUrl
    val winHandleBefore = driver.windowHandle
    driver.get(baseUrl.removeSuffix("jsp/Default.jsp"))
    val newHandle = driver.windowHandles.first { it != winHandleBefore }
    driver.switchTo().window(winHandleBefore)
    driver.close()
    driver.switchTo().window(newHandle)
    var enterprises: List<Enterprise> = emptyList()
    Sulfur(driver, listOf("Main", "Content")) {
      name("userNameField") += preferencesController.bmzefUsername
      name("passwortField") += preferencesController.bmzefPassword
      name("submitButton").submit()
      href("zeitenErfassen").submit()
      fun OptionElement.isValidOption() = this.text != "-- bitte selektieren --" && this.text.isNotBlank()
      enterprises = name("vorhabenComboSelected")
        .options
        .filter { it.isValidOption() }
        .map { vorhabenOption ->
          vorhabenOption.submit()
          val contracts = name("vertragComboSelected")
            .options
            .filter { it.isValidOption() }
            .map { vertragOption ->
              vertragOption.submit()
              val arten = name("taetigkeitsartComboSelected")
                .options
                .filter { it.isValidOption() }
                .map { artOption ->
                  artOption.submit()
                  val activities = name("taetigkeitComboSelected")
                    .options
                    .filter { it.isValidOption() }
                    .map { Activity(it.text) }
                  Kind(artOption.text, activities)
                }
              Contract(vertragOption.text, arten)
            }
          Enterprise(vorhabenOption.text, contracts)
        }
    }
    driver.quit()
    return enterprises
  }

}
