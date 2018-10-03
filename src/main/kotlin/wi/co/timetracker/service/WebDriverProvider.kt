package wi.co.timetracker.service

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import tornadofx.*

class WebDriverProvider: Controller() {

  init {
    WebDriverManager.chromedriver().setup()
  }

  fun createDriver(): WebDriver {
    return ChromeDriver()
  }

}
