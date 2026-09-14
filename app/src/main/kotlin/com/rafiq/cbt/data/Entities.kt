package com.rafiq.cbt.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "moods")
data class MoodEntry(
    @PrimaryKey val id: String,
    val date: Long,
    val value: Int,
    val note: String
)

@Entity(tableName = "thoughts")
data class ThoughtRecord(
    @PrimaryKey val id: String,
    val date: Long,
    val situation: String,
    val emotion: String,
    val intensityBefore: Int,
    val thought: String,
    val distortionIds: String,
    val forEv: String,
    val againstEv: String,
    val balanced: String,
    val intensityAfter: Int
)

@Entity(tableName = "downward_chains")
data class DownwardChain(
    @PrimaryKey val id: String,
    val date: Long,
    val chainJson: String,
    val core: String
)

@Entity(tableName = "activities")
data class ActivityEntry(
    @PrimaryKey val id: String,
    val date: Long,
    val activity: String,
    val category: String,
    val expPleasure: Int,
    val expMastery: Int,
    val done: Boolean,
    val actPleasure: Int?,
    val actMastery: Int?
)

@Entity(tableName = "experiments")
data class ExperimentEntry(
    @PrimaryKey val id: String,
    val date: Long,
    val belief: String,
    val confBefore: Int,
    val plan: String,
    val prediction: String,
    val outcome: String?,
    val confAfter: Int?,
    val learned: String?
)

@Entity(tableName = "exposures")
data class ExposureEntry(
    @PrimaryKey val id: String,
    val date: Long,
    val situation: String,
    val anxiety: Int,
    val completed: Boolean,
    val actualAnxiety: Int?
)

@Entity(tableName = "problems")
data class ProblemEntry(
    @PrimaryKey val id: String,
    val date: Long,
    val problem: String,
    val solutions: String,
    val prosCons: String,
    val chosen: String,
    val plan: String,
    val outcome: String
)

@Entity(tableName = "worries")
data class WorryEntry(
    @PrimaryKey val id: String,
    val date: Long,
    val text: String,
    val resolved: Boolean
)

@Entity(tableName = "relapse_plan")
data class RelapsePlanEntity(
    @PrimaryKey val id: Int = 1,
    val warning: String,
    val triggers: String,
    val strategies: String,
    val support: String,
    val firstStep: String
)

@Entity(tableName = "settings")
data class SettingEntry(
    @PrimaryKey val key: String,
    val value: String
)
