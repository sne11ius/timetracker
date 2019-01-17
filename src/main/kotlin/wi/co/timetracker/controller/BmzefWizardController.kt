package wi.co.timetracker.controller

import arrow.core.Either
import com.fasterxml.jackson.module.kotlin.readValue
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.stage.StageStyle
import mu.KotlinLogging
import tornadofx.*
import wi.co.timetracker.extensions.checked
import wi.co.timetracker.model.parser.hasErrors
import wi.co.timetracker.model.summary.DaySummaryModel
import wi.co.timetracker.service.FileLoader
import wi.co.timetracker.service.mapper
import wi.co.timetracker.service.mbzef.BmzefClient
import wi.co.timetracker.service.mbzef.BmzefService
import wi.co.timetracker.view.bmzef.BmzefWizard
import wi.co.timetracker.view.bmzef.BmzefWizardData
import wi.co.timetracker.view.bmzef.WaitController
import wi.co.timetracker.view.bmzef.WaitDialog
import java.io.File
import java.time.LocalDate
import kotlin.concurrent.thread

class BmzefWizardController : Controller() {

  private val logger = KotlinLogging.logger {}

  private val bmzefService: BmzefService by inject()
  private val model: BmzefWizardData by inject()
  private val preferencesController: PreferencesController by inject()
  private val waitController: WaitController by inject()
  private val fileLoader: FileLoader by di()
  private val bmzefClient: BmzefClient by inject()

  fun runTimeTracking() {
    model.beginDate = LocalDate.now().minusDays(1)
    model.endDate = LocalDate.now()
    loadEnterprises({
      find<BmzefWizard>() {
        openModal()
      }
    })
  }

  fun reloadEnterprises() {
    loadEnterprises({}, true)
  }

  private fun loadEnterprises(andThen: () -> Unit, forceUpdate: Boolean = false) {
    waitController.progressProperty().value = 0
    find<WaitDialog>() {
      openModal(stageStyle = StageStyle.UNDECORATED)
      thread {
        val enterprisesCacheFile = File(preferencesController.baseDir, ".timetracker.projectcache.json")
        if (forceUpdate || !enterprisesCacheFile.exists()) {
          val enterpriseCount = bmzefClient.readEnterpriseCount()
          var count = 0.0
          val allEnterpriseses = bmzefClient.readEnterprisesFromWeb {
            count += 1
            Platform.runLater {
              val currentProgress: Double = count / enterpriseCount
              waitController.progressProperty().value = currentProgress
            }
          }
          Platform.runLater {
            close()
            model.avalailabledEnterprises = when (allEnterpriseses) {
              is Either.Left -> throw RuntimeException(allEnterpriseses.a)
              is Either.Right -> allEnterpriseses.b
            }
            mapper.writeValue(enterprisesCacheFile, model.avalailabledEnterprises)
            andThen()
          }
        } else {
          model.avalailabledEnterprises = mapper.readValue(enterprisesCacheFile)
          val enterpriseCount = model.avalailabledEnterprises.size
          for (i in 1..enterpriseCount) {
            Platform.runLater {
              val currentProgress: Double = i.toDouble() / enterpriseCount
              waitController.progressProperty().value = currentProgress
            }
            Thread.sleep(10)
          }
          Platform.runLater {
            close()
            andThen()
          }
        }
      }
    }
  }

  fun reloadEntries() {
    if (model.beginDate.isAfter(model.endDate)) {
      val end = model.endDate
      model.endDate = model.beginDate
      model.beginDate = end
    }
    val begin = model.beginDate
    val end = model.endDate
    var currentDay = begin
    var models = listOf<DaySummaryModel>()
    while (currentDay != end.plusDays(1)) {
      val (_, errors, entry) = fileLoader.loadDay(currentDay, preferencesController.baseDir)
      if (errors.hasErrors) {
        Alert(Alert.AlertType.ERROR, "Datei für $currentDay ist leider kaputt.").showAndWait()
        throw RuntimeException()
      }
      if (entry != null) {
        models += with (preferencesController) {
          entry.toDaySummaryModel(
            breakIndicators,
            travelIndicators,
            travelMultiplier
          )
        }
      }
      currentDay = currentDay.plusDays(1)
    }
    models = models
      .map { m -> m.copy(entries = m.entries.filter { entry -> preferencesController.bmzefIgnoreIndicators.none { it == entry.text } }) }
      .filter { m -> m.entries.isNotEmpty() }
      .toMutableList()
    val allEntries = models.flatMap { it.entries }.map { it.text }.toSet()
    model.projectMapping *= bmzefService.loadMapping(allEntries)
    with (model) {
      val entries = allEntries.map { if (projectMapping.unmappedEntries.contains(it)) it else it.checked }.toList().sorted()
      logger.debug { "${entries.size} entries for ${model.beginDate} - ${model.endDate}" }
      entryTexts.setAll(entries)
    }
  }

}
