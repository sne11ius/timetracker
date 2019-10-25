package wi.co.timetracker.service.mbzef

import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import tornadofx.Controller
import wi.co.timetracker.controller.PreferencesController
import wi.co.timetracker.model.bmzef.ActivityPath
import wi.co.timetracker.model.bmzef.ActivityPathPart
import wi.co.timetracker.model.bmzef.ProjectMapping
import wi.co.timetracker.model.summary.DaySummaryModel
import wi.co.timetracker.service.mapper

class BmzefService : Controller() {

  private val preferencesController: PreferencesController by inject()
  private val bmzefClient: BmzefClient by inject()

  fun isValid(path: ActivityPath, avalailabledEnterprises: Set<ActivityPathPart.Enterprise>): Boolean {
    return when (path) {
      is ActivityPath.NoPath -> false
      is ActivityPath.Path -> {
        val possiblePaths = avalailabledEnterprises.flatMap { it.paths }
        possiblePaths.contains(path)
      }
    }
  }

  fun updateProjectMappingCache(projectMapping: ProjectMapping) {
    val cachedMapping: ProjectMapping = loadCachedMapping()
    val updatedMapping = projectMapping and cachedMapping
    val mappingCacheFile = File(preferencesController.baseDir, ".timetracker.mappingcache.json")
    mapper.writeValue(mappingCacheFile, updatedMapping)
  }

  private fun loadCachedMapping(): ProjectMapping {
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

  fun commit(summaryModels: List<DaySummaryModel>, projectMapping: ProjectMapping) {
    summaryModels.forEach {
      bmzefClient.commit(it, projectMapping)
    }
  }
}
