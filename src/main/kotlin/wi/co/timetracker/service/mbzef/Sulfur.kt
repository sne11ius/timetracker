package wi.co.timetracker.service.mbzef

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.Select

data class WrappedElement(
  val d: WebDriver,
  val sel: String,
  val preferredFrames: List<String>? = null
) {
  val options: Set<OptionElement>
    get() {
      return els(sel + " option").map { OptionElement(d, sel, it.text, preferredFrames) }.toSet()
    }

  operator fun plusAssign(text: String) {
    el(sel).sendKeys(text)
  }

  fun click() {
    el(sel).click()
  }

  fun submit() {
    click()
    switchToFrame(d, preferredFrames)
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
  val text: String,
  val preferredFrames: List<String>?
) {

  fun click() {
    val el = d.findElement(By.cssSelector(parentSel))
    Select(el).selectByVisibleText(text)
  }

  fun submit() {
    click()
    switchToFrame(d, preferredFrames)
  }

}

class Sulfur(
  private val driver: WebDriver,
  private val preferredFrames: List<String>? = null,
  private val block: (Sulfur.() -> Unit)? = null
) {

  init {
    switchToFrame(driver, preferredFrames)
    if (block != null)
      block.invoke(this)
  }

  fun el(sel: String): WrappedElement {
    return WrappedElement(driver, sel, preferredFrames)
  }

  fun name(name: String): WrappedElement {
    return el("[name=\"$name\"]")
  }

  fun href(text: String): WrappedElement {
    return el("[href*='$text']")
  }

}

private fun switchToFrame(d: WebDriver, preferredFrames: List<String>?) {
  preferredFrames?.forEach { f ->
    try {
      d.switchTo().frame(f)
      return
    }
    catch (e: Exception) {
      // miss me with that shit
    }
  }
}
