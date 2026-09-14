package com.rafiq.cbt

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rafiq.cbt.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = (app as CbtApplication).database.dao()

    val moods = dao.getMoods().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val thoughts = dao.getThoughts().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val downward = dao.getDownward().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val activities = dao.getActivities().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val experiments = dao.getExperiments().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val exposures = dao.getExposures().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val problems = dao.getProblems().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val worries = dao.getWorries().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val relapsePlan = dao.getRelapsePlan().stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val worryTimeSetting = dao.getSetting("worryTime").stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val biometricLockEnabled = dao.getSetting("biometricLock")
        .map { it?.value == "true" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private fun newId() = UUID.randomUUID().toString()

    fun addMood(value: Int, note: String) = viewModelScope.launch {
        dao.insertMood(MoodEntry(newId(), System.currentTimeMillis(), value, note))
    }
    fun deleteMood(id: String) = viewModelScope.launch { dao.deleteMood(id) }

    fun addThought(
        situation: String, emotion: String, intensityBefore: Int, thought: String,
        distortionIds: List<String>, forEv: String, againstEv: String, balanced: String, intensityAfter: Int
    ) = viewModelScope.launch {
        dao.insertThought(
            ThoughtRecord(
                newId(), System.currentTimeMillis(), situation, emotion, intensityBefore,
                thought, distortionIds.joinToString(","), forEv, againstEv, balanced, intensityAfter
            )
        )
    }
    fun deleteThought(id: String) = viewModelScope.launch { dao.deleteThought(id) }

    fun addDownwardChain(chain: List<String>) = viewModelScope.launch {
        if (chain.isEmpty()) return@launch
        dao.insertDownward(DownwardChain(newId(), System.currentTimeMillis(), encodeChain(chain), chain.last()))
    }
    fun deleteDownward(id: String) = viewModelScope.launch { dao.deleteDownward(id) }

    fun addActivity(activity: String, category: String, expPleasure: Int, expMastery: Int) = viewModelScope.launch {
        dao.insertActivity(ActivityEntry(newId(), System.currentTimeMillis(), activity, category, expPleasure, expMastery, false, null, null))
    }
    fun completeActivity(a: ActivityEntry, actPleasure: Int, actMastery: Int) = viewModelScope.launch {
        dao.updateActivity(a.copy(done = true, actPleasure = actPleasure, actMastery = actMastery))
    }
    fun deleteActivity(id: String) = viewModelScope.launch { dao.deleteActivity(id) }

    fun addExperiment(belief: String, confBefore: Int, plan: String, prediction: String) = viewModelScope.launch {
        dao.insertExperiment(ExperimentEntry(newId(), System.currentTimeMillis(), belief, confBefore, plan, prediction, null, null, null))
    }
    fun completeExperiment(e: ExperimentEntry, outcome: String, confAfter: Int, learned: String) = viewModelScope.launch {
        dao.updateExperiment(e.copy(outcome = outcome, confAfter = confAfter, learned = learned))
    }
    fun deleteExperiment(id: String) = viewModelScope.launch { dao.deleteExperiment(id) }

    fun addExposure(situation: String, anxiety: Int) = viewModelScope.launch {
        dao.insertExposure(ExposureEntry(newId(), System.currentTimeMillis(), situation, anxiety, false, null))
    }
    fun completeExposure(x: ExposureEntry, actualAnxiety: Int) = viewModelScope.launch {
        dao.updateExposure(x.copy(completed = true, actualAnxiety = actualAnxiety))
    }
    fun deleteExposure(id: String) = viewModelScope.launch { dao.deleteExposure(id) }

    fun addProblem(problem: String, solutions: String, prosCons: String, chosen: String, plan: String, outcome: String) = viewModelScope.launch {
        dao.insertProblem(ProblemEntry(newId(), System.currentTimeMillis(), problem, solutions, prosCons, chosen, plan, outcome))
    }
    fun deleteProblem(id: String) = viewModelScope.launch { dao.deleteProblem(id) }

    fun addWorry(text: String) = viewModelScope.launch {
        dao.insertWorry(WorryEntry(newId(), System.currentTimeMillis(), text, false))
    }
    fun resolveWorry(w: WorryEntry) = viewModelScope.launch { dao.updateWorry(w.copy(resolved = true)) }
    fun deleteWorry(id: String) = viewModelScope.launch { dao.deleteWorry(id) }

    fun saveWorryTime(time: String) = viewModelScope.launch { dao.setSetting(SettingEntry("worryTime", time)) }

    fun saveRelapsePlan(warning: String, triggers: String, strategies: String, support: String, firstStep: String) = viewModelScope.launch {
        dao.saveRelapsePlan(RelapsePlanEntity(1, warning, triggers, strategies, support, firstStep))
    }

    fun setBiometricLock(enabled: Boolean) = viewModelScope.launch {
        dao.setSetting(SettingEntry("biometricLock", if (enabled) "true" else "false"))
    }

    fun exportData(): ExportBundle = ExportBundle(
        moods.value, thoughts.value, downward.value, activities.value, experiments.value,
        exposures.value, problems.value, worries.value, worryTimeSetting.value?.value ?: "", relapsePlan.value
    )

    fun importData(bundle: ExportBundle) = viewModelScope.launch {
        dao.clearMoods(); dao.clearThoughts(); dao.clearDownward(); dao.clearActivities()
        dao.clearExperiments(); dao.clearExposures(); dao.clearProblems(); dao.clearWorries()
        dao.clearRelapsePlan()
        bundle.moods.forEach { dao.insertMood(it) }
        bundle.thoughts.forEach { dao.insertThought(it) }
        bundle.downward.forEach { dao.insertDownward(it) }
        bundle.activities.forEach { dao.insertActivity(it) }
        bundle.experiments.forEach { dao.insertExperiment(it) }
        bundle.exposures.forEach { dao.insertExposure(it) }
        bundle.problems.forEach { dao.insertProblem(it) }
        bundle.worries.forEach { dao.insertWorry(it) }
        if (bundle.worryTime.isNotBlank()) dao.setSetting(SettingEntry("worryTime", bundle.worryTime))
        bundle.relapsePlan?.let { dao.saveRelapsePlan(it) }
    }

    fun resetAll() = viewModelScope.launch {
        dao.clearMoods(); dao.clearThoughts(); dao.clearDownward(); dao.clearActivities()
        dao.clearExperiments(); dao.clearExposures(); dao.clearProblems(); dao.clearWorries()
        dao.clearRelapsePlan(); dao.clearSettings()
    }
}
