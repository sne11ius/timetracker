package wi.co.timetracker.model.bmzef

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

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
        .toSet() + EntryMapping(mappedEntry.first, mappedEntry.second),
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
