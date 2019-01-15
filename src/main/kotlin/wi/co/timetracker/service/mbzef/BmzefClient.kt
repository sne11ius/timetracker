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
import wi.co.timetracker.extensions.pflatMap
import wi.co.timetracker.model.bmzef.ActivityPathPart
import java.time.Duration
import java.time.LocalDateTime.now

private class Sulfur(val baseUrl: String) {
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

  internal fun Document.frameSource(frameIndex: Int): String {
    return select("frame")[frameIndex].attr("src")
  }

  internal fun Document.frameSource(frameName: String): String {
    return select("frame[name=\"$frameName\"]").attr("src")
  }

  internal fun Document.formAction(formIndex: Int): String {
    return select("form")[formIndex].attr("action")
  }

  internal fun Document.options(name: String): List<Pair<String, String>> {
    return select("select[name=\"$name\"] option").map { el ->
      Pair(el.attr("value"), el.text())
    }
  }
}

class BmzefClient : Controller() {

  private val EVENT ="REQUEST.EVENT"

  private val preferencesController: PreferencesController by inject()

  private val logger = KotlinLogging.logger {}

  private val Pair<String, String>.isValid: Boolean
    get() = this.second != "-- bitte selektieren --" && this.second.isNotBlank()

  private fun Sulfur.loadMainPage(): Document {
    val username = preferencesController.bmzefUsername
    val password = preferencesController.bmzefPassword
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
      EVENT, "login",
      "userNameField", username,
      "passwortField", password
    )
    val auswahlLink = selectActionPage.frameSource(1)
    val auswahlPage = get(auswahlLink)
    val zeitenErfassenLink = auswahlPage.formAction(0)
    val zeitenErfassenPage = post(
      zeitenErfassenLink,
      EVENT, "zeitenErfassen"
    )
    val mainFormLink = zeitenErfassenPage.frameSource("Content")
    return get(mainFormLink)
  }

  private fun Sulfur.postMainForm(document: Document, event: String, fieldName: String, fieldValue: String): Document {
    val postPath = document.formAction(0)
    val postUrl = "$baseUrl$postPath"
    val mainForm = post(postUrl,
      EVENT, event,
      fieldName, fieldValue
    )
    val mainFormLink = mainForm.frameSource("Content")
    return get(mainFormLink)
  }

  private fun Duration.prettyPrint(): String = this.toString()
    .substring(2)
    .replace(Regex("(\\d[HMS])(?!$)"), "$1 ")
    .toLowerCase()

  fun readEnterprisesFromWeb(): Either<String, Set<ActivityPathPart.Enterprise>> {
    try {
      val start = now()
      val enterpriseNames = readEnterpriseNames()
      logger.debug { "${enterpriseNames.size} enterprises found" }
      // Tatsächlich ist es am schnellsten, wenn wir alle Daten möglichst parallel auslesen. Hiermit werden ca. 2
      // vollständige Vorhaben pro Sekunde ausgelesen.
      val chunkSize = 1
      val enterprises = enterpriseNames
        .chunked(chunkSize)
        .pflatMap { names ->
          readEnterprises(names)
        }
        .toSet()
      val end = now()
      val time = Duration.between(start, end)
      logger.debug { "Reading all enterprises took ${time.prettyPrint()}" }
      return Right(enterprises)
    } catch (e: Exception) {
      logger.error(e.localizedMessage, e)
      return Left("Konnte Projekte nicht aus bmzef lesen: ${e.localizedMessage}")
    }
  }

  private fun readEnterpriseNames(): List<String> {
    logger.debug { "Reading enterprise names..." }
    val baseUrl = preferencesController.bmzefBaseUrl
    return Sulfur(baseUrl).run {
      val result = loadMainPage().options("vorhabenComboSelected").map { it.second }
      logger.debug { "...done" }
      result
    }
  }

  private fun readEnterprises(enterpriseNames: List<String>): Set<ActivityPathPart.Enterprise> {
    val baseUrl = preferencesController.bmzefBaseUrl

    return Sulfur(baseUrl).run {
      var mainForm = loadMainPage()
      val vorhabenOptions = mainForm.options("vorhabenComboSelected")
      vorhabenOptions
        .filter { it.isValid }
        .filter { it.second in enterpriseNames }
        .map { (vorhabenValue, vorhabenName) ->
          mainForm = postMainForm(mainForm, "vorhabenSelektiert", "vorhabenComboSelected", vorhabenValue)
          val vertragOptions = mainForm.options("vertragComboSelected")
          val contracts = vertragOptions
            .filter { it.isValid }
            .map { (vertragValue, vertragName) ->
              mainForm = postMainForm(mainForm, "vertragSelektiert", "vertragComboSelected", vertragValue)
              val taetigkeitsartOptions = mainForm.options("taetigkeitsartComboSelected")
              val kinds = taetigkeitsartOptions
                .filter { it.isValid }
                .map { (taetigkeitsartValue, taetigkeitsartName) ->
                  mainForm = postMainForm(mainForm, "taetigkeitsartSelektiert", "taetigkeitsartComboSelected", taetigkeitsartValue)
                  val taetigkeitOptions = mainForm.options("taetigkeitComboSelected")
                  val activities = taetigkeitOptions
                    .filter { it.isValid }
                    .map { (_, activityName) ->
                      ActivityPathPart.Activity(activityName)
                    }
                    .toSet()
                  ActivityPathPart.Kind(taetigkeitsartName, activities)
                }
                .toSet()
              ActivityPathPart.Contract(vertragName, kinds)
            }.toSet()
          ActivityPathPart.Enterprise(vorhabenName, contracts)
        }.toSet()
    }
  }
}
