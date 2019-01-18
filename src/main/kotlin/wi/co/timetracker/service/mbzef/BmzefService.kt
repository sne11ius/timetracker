package wi.co.timetracker.service.mbzef

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.readValue
import tornadofx.*
import wi.co.timetracker.controller.PreferencesController
import wi.co.timetracker.model.bmzef.ActivityPath
import wi.co.timetracker.model.bmzef.ActivityPathPart
import wi.co.timetracker.service.mapper
import java.io.File

class BmzefService: Controller() {

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

    operator fun plus(mappedEntry: Pair<String, ActivityPath.Path>): ProjectMapping {
      return copy(
        mappedEntries = mappedEntries
          .filter { it.entryText != mappedEntry.first }
          .toSet() + BmzefService.EntryMapping(mappedEntry.first, mappedEntry.second),
        unmappedEntries = unmappedEntries - mappedEntry.first
      )
    }

    operator fun minus(unmappedEntryText: String): ProjectMapping {
      return copy(
        mappedEntries = mappedEntries.filter { it.entryText != unmappedEntryText }.toSet(),
        unmappedEntries = unmappedEntries + unmappedEntryText
      )
    }

    @JsonIgnore
    val isComplete = unmappedEntries.isEmpty()
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
