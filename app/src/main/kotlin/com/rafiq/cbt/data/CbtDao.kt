package com.rafiq.cbt.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CbtDao {

    @Query("SELECT * FROM moods ORDER BY date ASC")
    fun getMoods(): Flow<List<MoodEntry>>
    @Insert suspend fun insertMood(m: MoodEntry)
    @Query("DELETE FROM moods WHERE id = :id") suspend fun deleteMood(id: String)

    @Query("SELECT * FROM thoughts ORDER BY date DESC")
    fun getThoughts(): Flow<List<ThoughtRecord>>
    @Insert suspend fun insertThought(t: ThoughtRecord)
    @Query("DELETE FROM thoughts WHERE id = :id") suspend fun deleteThought(id: String)

    @Query("SELECT * FROM downward_chains ORDER BY date DESC")
    fun getDownward(): Flow<List<DownwardChain>>
    @Insert suspend fun insertDownward(d: DownwardChain)
    @Query("DELETE FROM downward_chains WHERE id = :id") suspend fun deleteDownward(id: String)

    @Query("SELECT * FROM activities ORDER BY date DESC")
    fun getActivities(): Flow<List<ActivityEntry>>
    @Insert suspend fun insertActivity(a: ActivityEntry)
    @Update suspend fun updateActivity(a: ActivityEntry)
    @Query("DELETE FROM activities WHERE id = :id") suspend fun deleteActivity(id: String)

    @Query("SELECT * FROM experiments ORDER BY date DESC")
    fun getExperiments(): Flow<List<ExperimentEntry>>
    @Insert suspend fun insertExperiment(e: ExperimentEntry)
    @Update suspend fun updateExperiment(e: ExperimentEntry)
    @Query("DELETE FROM experiments WHERE id = :id") suspend fun deleteExperiment(id: String)

    @Query("SELECT * FROM exposures ORDER BY anxiety ASC")
    fun getExposures(): Flow<List<ExposureEntry>>
    @Insert suspend fun insertExposure(x: ExposureEntry)
    @Update suspend fun updateExposure(x: ExposureEntry)
    @Query("DELETE FROM exposures WHERE id = :id") suspend fun deleteExposure(id: String)

    @Query("SELECT * FROM problems ORDER BY date DESC")
    fun getProblems(): Flow<List<ProblemEntry>>
    @Insert suspend fun insertProblem(p: ProblemEntry)
    @Query("DELETE FROM problems WHERE id = :id") suspend fun deleteProblem(id: String)

    @Query("SELECT * FROM worries ORDER BY date DESC")
    fun getWorries(): Flow<List<WorryEntry>>
    @Insert suspend fun insertWorry(w: WorryEntry)
    @Update suspend fun updateWorry(w: WorryEntry)
    @Query("DELETE FROM worries WHERE id = :id") suspend fun deleteWorry(id: String)

    @Query("SELECT * FROM relapse_plan WHERE id = 1")
    fun getRelapsePlan(): Flow<RelapsePlanEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveRelapsePlan(r: RelapsePlanEntity)

    @Query("SELECT * FROM settings WHERE `key` = :k")
    fun getSetting(k: String): Flow<SettingEntry?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun setSetting(s: SettingEntry)

    @Query("DELETE FROM moods") suspend fun clearMoods()
    @Query("DELETE FROM thoughts") suspend fun clearThoughts()
    @Query("DELETE FROM downward_chains") suspend fun clearDownward()
    @Query("DELETE FROM activities") suspend fun clearActivities()
    @Query("DELETE FROM experiments") suspend fun clearExperiments()
    @Query("DELETE FROM exposures") suspend fun clearExposures()
    @Query("DELETE FROM problems") suspend fun clearProblems()
    @Query("DELETE FROM worries") suspend fun clearWorries()
    @Query("DELETE FROM relapse_plan") suspend fun clearRelapsePlan()
    @Query("DELETE FROM settings") suspend fun clearSettings()
}
