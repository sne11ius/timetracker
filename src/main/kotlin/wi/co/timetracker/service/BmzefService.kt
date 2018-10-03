package wi.co.timetracker.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tornadofx.*
import wi.co.timetracker.controller.PreferencesController
import wi.co.timetracker.model.bmzef.Enterprise

class BmzefService: Controller() {

  private val preferencesController: PreferencesController by inject()

  private val driverProvider: WebDriverProvider by inject()

  fun readAvailableEnterprises(): List<Enterprise> {
    val driver = driverProvider.createDriver()
    val baseUrl = preferencesController.bmzefBaseUrl
    val winHandleBefore = driver.windowHandle
    driver.get(baseUrl.removeSuffix("jsp/Default.jsp"))
    //val newHandle = driver.windowHandle
    //driver.switchTo().window(winHandleBefore)
    //driver.close()
    //driver.switchTo().window(newHandle)
    // driver.get(baseUrl)
    with (Sulfur(driver)) {
      name("userNameField") += preferencesController.bmzefUsername
      name("passwortField") += preferencesController.bmzefPassword
      name("submitButton").click()
      href("zeitenErfassen").click()
      name("vorhabenComboSelected").options.filter { it.text != "-- bitte selektieren --" }.map { option ->
        option.click()
      }
    }
    driver.quit()
    TODO("Not yet.")
  }

}
