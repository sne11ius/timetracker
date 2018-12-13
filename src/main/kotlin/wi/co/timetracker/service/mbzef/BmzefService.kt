package wi.co.timetracker.service.mbzef

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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
    return if (forceUpdate || !enterprisesCacheFile.exists()) {
      val availableEnterprises = readEnterprisesFromWeb()
      mapper.writeValue(enterprisesCacheFile, availableEnterprises)
      availableEnterprises
    } else
      mapper.readValue(enterprisesCacheFile)
  }

  @JsonIgnoreProperties(value = ["complete", "incomplete"])
  data class ProjectMapping(
    val mappedEntries: Set<EntryMapping> = emptySet(),
    val unmappedEntries: Set<String> = emptySet()
  ) {
    operator fun times(more: ProjectMapping): ProjectMapping {
      return ProjectMapping(
        more.mappedEntries.filter { mappedEntries.none { known -> known.entryText == it.entryText } }.toSet(),
        more.unmappedEntries.filter { mappedEntries.none { known -> known.entryText == it } }.toSet()
      )
    }

    fun pathFor(entry: String?): ActivityPath = mappedEntries.firstOrNull { it.entryText == entry }?.activity ?: ActivityPath.NoPath

    infix fun and(more: ProjectMapping): ProjectMapping {
      return ProjectMapping(
        mappedEntries + more.mappedEntries.filter { mappedEntries.none { known -> known.entryText == it.entryText } }.toSet()
      )
    }

    @JsonIgnore
    val isComplete = unmappedEntries.isEmpty()
    @JsonIgnore
    val isIncomplete = !isComplete
    @JsonIgnore
    val texts = mappedEntries.map { it.entryText } + unmappedEntries
  }

  data class EntryMapping(
    val entryText: String,
    val activity: ActivityPath.Path
  )

  data class MappingCache(
    val mappedEntries: Set<EntryMapping>
  ) {
    val texts = mappedEntries.map { it.entryText }
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

  fun updateProjectMappingCache(projectMapping: BmzefService.ProjectMapping) {
    val cachedMapping: ProjectMapping = loadCachedMapping()
    val updatedMapping = projectMapping and cachedMapping
    val mappingCacheFile = File(preferencesController.baseDir, ".timetracker.mappingcache.json")
    mapper.writeValue(mappingCacheFile, updatedMapping)
  }

  private fun loadCachedMapping(): BmzefService.ProjectMapping {
    val mappingCacheFile = File(preferencesController.baseDir, ".timetracker.mappingcache.json")
    if (!mappingCacheFile.exists()) {
      return ProjectMapping()
    }
    return mapper.readValue(mappingCacheFile)
  }

  fun loadMapping(allEntries: Set<String>): ProjectMapping {
    val cachedMapping = loadCachedMapping()
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

}
