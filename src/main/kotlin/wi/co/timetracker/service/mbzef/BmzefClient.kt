package wi.co.timetracker.service.mbzef

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import mu.KotlinLogging
import org.jsoup.Connection
import org.jsoup.Jsoup.connect
import org.jsoup.nodes.Document
import tornadofx.*
import wi.co.timetracker.controller.PreferencesController
import wi.co.timetracker.model.bmzef.ActivityPathPart

private class Bmzef(val baseUrl: String) {
  private var cookies: Map<String, String> = emptyMap()

  internal fun get(url: String): Document {
    val finalUrl = if (url.startsWith(baseUrl)) url else "$baseUrl$url"
    val response = connect(finalUrl)
      .method(Connection.Method.GET)
      .cookies(cookies)
      .execute()
    cookies += response.cookies()
    return response.parse()
  }

  internal fun post(url: String, vararg params: String): Document {
    val finalUrl = if (url.startsWith(baseUrl)) url else "$baseUrl$url"
    val response = connect(finalUrl)
      .method(Connection.Method.POST)
      .cookies(cookies)
      .data(*params)
      .execute()
    cookies += response.cookies()
    return response.parse()
  }

  internal fun Document.frameSource(index: Int): String {
    return select("frame")[index].attr("src")
  }

  internal fun Document.frameSource(name: String): String {
    return select("frame[name=\"$name\"]").attr("src")
  }

  internal fun Document.formAction(index: Int): String {
    return select("form")[index].attr("action")
  }
}

class BmzefClient : Controller() {

  private val preferencesController: PreferencesController by inject()

  private val logger = KotlinLogging.logger {}

  private val Pair<String, String>.isValid: Boolean
    get() = this.second != "-- bitte selektieren --" && this.second.isNotBlank()

  fun readEnterprisesFromWeb(): Either<String, Set<ActivityPathPart.Enterprise>> {
    val baseUrl = preferencesController.bmzefBaseUrl
    val username = preferencesController.bmzefUsername
    val password =  preferencesController.bmzefPassword
    try {
      Bmzef(baseUrl).run {
        // Do the "setup session cookies" dance
        get("/bmzef")
        get("/bmzef/jsp/index.jsp")
        // Do the "login" dance
        val defaultPage = get("/bmzef/jsp/Default.jsp")
        val loginLink = defaultPage.frameSource(1)
        val loginPage = get(loginLink)
        val loginPostLink = loginPage.formAction(0)
        val selectActionPage = post(
          loginPostLink,
          "REQUEST.EVENT", "login",
          "userNameField", username,
          "passwortField", password
        )
        val auswahlLink = selectActionPage.frameSource(1)
        val auswahlPage = get(auswahlLink)
        val zeitenErfassenLink = auswahlPage.formAction(0)
        val zeitenErfassenPage = post(
          zeitenErfassenLink,
          "REQUEST.EVENT", "zeitenErfassen"
        )
        var mainFormLink = zeitenErfassenPage.frameSource("Content")
        var mainForm = get(mainFormLink)
        val options = mainForm.select("select[name=\"vorhabenComboSelected\"] option").map { el ->
          Pair(el.attr("value"), el.text())
        }
        val enterprises = options
          .filter { it.isValid }
          .map { (value, name) ->
            println("POSTing $name")
            val postPath = mainForm.formAction(0)
            val postUrl = "$baseUrl$postPath"
            mainForm = post(postUrl,
              "REQUEST.EVENT", "vorhabenSelektiert",
              "vorhabenComboSelected", value
            )
            mainFormLink = mainForm.frameSource("Content")
            mainForm = get(mainFormLink)
            val contracts = mainForm.select("select[name=\"vertragComboSelected\"] option").map { el ->
              // Pair(el.attr("value"), el.text())
              ActivityPathPart.Contract(el.text(), emptySet())
            }.toSet()
            println("Verträge für $name: $contracts")
            ActivityPathPart.Enterprise(name, contracts)
          }
          .toSet()
        return Right(enterprises)
      }
    } catch (e: Exception) {
      logger.error(e.localizedMessage, e)
      return Left("Konnte Projekte nicht aus bmzef lesen: ${e.localizedMessage}")
    }
  }
}
