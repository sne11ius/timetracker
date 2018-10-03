package wi.co.timetracker.model.bmzef

// Die Namen sind mehr oder weniger einfache Übersetzungen - inhaltlich sollte man da (bis auf die Hierarchie) nicht
// zu viel reindeuten.

data class Activity(
  val title: String
)

data class Kind(
  val title: String,
  val activities: List<Activity>
)

data class Contract(
  val title: String,
  val kinds: List<Kind>
)

data class Enterprise(
  val title: String,
  val contracts: List<Contract>
)
