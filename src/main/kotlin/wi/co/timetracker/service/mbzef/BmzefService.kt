package wi.co.timetracker.service.mbzef

import com.fasterxml.jackson.module.kotlin.readValue
import tornadofx.*
import wi.co.timetracker.controller.PreferencesController
import wi.co.timetracker.model.bmzef.*
import wi.co.timetracker.service.mapper
import java.io.File

class BmzefService: Controller() {

  private val preferencesController: PreferencesController by inject()

  private val driverProvider: WebDriverProvider by inject()

  fun isValid(path: ActivityPath): Boolean {
    return when (path) {
      is ActivityPath.NoPath -> false
      is ActivityPath.Path -> {
        val (enterprise, contract, kind, activity) = path
        val possiblePaths = readAvailableEnterprises().flatMap { it.paths }
        possiblePaths.contains(path)
      }
    }
  }

  fun readAvailableEnterprises(forceUpdate: Boolean = false): Set<ActivityPathPart.Enterprise> {
    val enterprisesCacheFile = File(preferencesController.baseDir, ".timetracker.projectcache.json")
    if (forceUpdate || !enterprisesCacheFile.exists()) {
      val availableEnterprises = readEnterprisesFromWeb()
      mapper.writeValue(enterprisesCacheFile, availableEnterprises)
      return availableEnterprises
    } else
      return mapper.readValue(enterprisesCacheFile)
  }

  data class ProjectMapping(
    val mappedEntries: Set<EntryMapping> = emptySet(),
    val unmappedEntries: Set<String> = emptySet()
  ) {
    val isComplete = unmappedEntries.isEmpty()
    val isIncomplete = !isComplete
    val texts = mappedEntries.map { it.entryText } + unmappedEntries
  }

  data class EntryMapping(
    val entryText: String,
    val activity: ActivityPath
  )

  data class MappingCache(
    val mappedEntries: Set<EntryMapping>
  ) {
    val texts = mappedEntries.map { it.entryText }
  }

  fun loadMapping(allEntries: Set<String>): ProjectMapping {
    val mappingCacheFile = File(preferencesController.baseDir, ".timetracker.mappingcache.json")
    if (!mappingCacheFile.exists()) {
      return ProjectMapping(emptySet(), allEntries)
    }
    val cachedMapping: MappingCache = mapper.readValue(mappingCacheFile)
    val mappedEntries = allEntries.filter { entry ->
        cachedMapping.texts.contains(entry)
      }.map { entry ->
        cachedMapping.mappedEntries.first { it.entryText == entry }
      }.toSet()
    val unmappedEntries = allEntries.filter { entry ->
      !cachedMapping.texts.contains(entry)
    }.toSet()
    return ProjectMapping(
      mappedEntries,
      unmappedEntries
    )
  }

  fun readEnterprisesFromWeb(): Set<ActivityPathPart.Enterprise> {
    val driver = driverProvider.createDriver()
    val baseUrl = preferencesController.bmzefBaseUrl
    val winHandleBefore = driver.windowHandle
    driver.get(baseUrl.removeSuffix("jsp/Default.jsp"))
    val newHandle = driver.windowHandles.first { it != winHandleBefore }
    driver.switchTo().window(winHandleBefore)
    driver.close()
    driver.switchTo().window(newHandle)
    var enterprises: Set<ActivityPathPart.Enterprise> = emptySet()
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
                    .map { ActivityPathPart.Activity(it.text) }
                    .toSet()
                  ActivityPathPart.Kind(artOption.text, activities)
                }
                .toSet()
              ActivityPathPart.Contract(vertragOption.text, arten)
            }
            .toSet()
          ActivityPathPart.Enterprise(vorhabenOption.text, contracts)
        }.toSet()
    }
    driver.quit()
    return enterprises
  }

}
