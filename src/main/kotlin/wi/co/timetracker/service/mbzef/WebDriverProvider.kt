package wi.co.timetracker.service.mbzef

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import tornadofx.*
import java.util.concurrent.TimeUnit

class WebDriverProvider: Controller() {

  init {
    WebDriverManager.chromedriver().setup()
  }

  fun createDriver(): WebDriver {
    val driver = ChromeDriver()
    driver.manage().timeouts().implicitlyWait(500, TimeUnit.MILLISECONDS)
    return driver
  }

}
