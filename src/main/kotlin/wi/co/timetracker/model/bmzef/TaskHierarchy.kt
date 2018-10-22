package wi.co.timetracker.model.bmzef

import java.lang.RuntimeException

// Die Namen sind mehr oder weniger einfache Übersetzungen - inhaltlich sollte man da (bis auf die Hierarchie) nicht
// zu viel reindeuten.
// Jedenfalls:
//   Vorhaben => Enterprise
//   Vertract => Contract
//   Tätigkeitsart => Kind
//   Tätigkeit => Activity

sealed class ActivityPathPart {
  abstract val title: String
  abstract val isComplete: Boolean

  data class Activity(
    override val title: String,
    override val isComplete: Boolean = true
  ): ActivityPathPart()

  data class Kind(
    override val title: String,
    val activities: Set<Activity>,
    override val isComplete: Boolean = activities.isEmpty()
  ): ActivityPathPart() {
    val paths: Set<Pair<String, String?>>
      get() {
        return if (activities.isEmpty())
          setOf(Pair(title, null))
        else
          activities.map { Pair(title, it.title) }.toSet()
      }
  }

  data class Contract(
    override val title: String,
    val kinds: Set<Kind>,
    override val isComplete: Boolean = kinds.isEmpty()
  ): ActivityPathPart() {
    val paths: Set<Triple<String, String?, String?>>
      get() {
        return if (kinds.isEmpty())
          setOf(Triple(title, null, null))
        else
          kinds.flatMap { kind ->
            kind.paths.map { (k, a) ->
              Triple(title, k, a)
            }
          }.toSet()
      }
  }

  data class Enterprise(
    override val title: String,
    val contracts: Set<Contract>,
    override val isComplete: Boolean = contracts.isEmpty()
  ): ActivityPathPart() {
    val paths: Set<ActivityPath>
      get() {
        return if (contracts.isEmpty())
          setOf(ActivityPath.Path(title))
        else
          contracts.flatMap { c ->
            c.paths.map { (c, k, a) ->
              ActivityPath.Path(title, c, k, a)
            }
          }.toSet()
      }
  }
}

fun Collection<ActivityPathPart>.sortedTitles(): List<String> {
  return map { it.title }.sorted()
}

sealed class ActivityPath {
  abstract fun toPath(): ActivityPath.Path

  object NoPath: ActivityPath() {
    override fun toPath() = throw RuntimeException("Cannot make a path from a NoPath")
  }
  data class Path(
    val enterprise: String,
    val contract: String? = null,
    val kind: String? = null,
    val activity: String? = null
  ): ActivityPath() {
    override fun toPath() = this
  }
}
