package wi.co.timetracker.service

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.Select

data class WrappedElement(
  val d: WebDriver,
  val sel: String
) {
  val options: List<OptionElement>
    get() {
      return els(sel + "option").map { OptionElement(d, sel, it.getAttribute("value")) }
    }

  operator fun plusAssign(text: String) {
    el(sel).sendKeys(text)
  }

  fun click() {
    el(sel).click()
  }

  private fun el(sel: String): WebElement {
    return d.findElement(By.cssSelector(sel))
  }

  private fun els(sel: String): List<WebElement> {
    return d.findElements(By.cssSelector(sel))
  }
}

data class OptionElement(
  val d: WebDriver,
  val parentSel: String,
  val value: String
) {
  val text: String
    get() {
      return d.findElement(By.cssSelector(parentSel + " [value='$value']")).text
    }

  fun click() {
    val el = d.findElement(By.cssSelector(parentSel))
    Select(el).selectByValue(value)
  }

}

class Sulfur(
  private val driver: WebDriver
) {

  fun el(sel: String): WrappedElement {
    return WrappedElement(driver, sel)
  }

  fun name(name: String): WrappedElement {
    return el("[name='$name']")
  }

  fun href(text: String): WrappedElement {
    return el("[href*='$text']")
  }

}
