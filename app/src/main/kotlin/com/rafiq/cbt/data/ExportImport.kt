package com.rafiq.cbt.data

import org.json.JSONArray
import org.json.JSONObject

data class ExportBundle(
    val moods: List<MoodEntry>,
    val thoughts: List<ThoughtRecord>,
    val downward: List<DownwardChain>,
    val activities: List<ActivityEntry>,
    val experiments: List<ExperimentEntry>,
    val exposures: List<ExposureEntry>,
    val problems: List<ProblemEntry>,
    val worries: List<WorryEntry>,
    val worryTime: String,
    val relapsePlan: RelapsePlanEntity?
)

private fun opt(o: JSONObject, key: String): String? = if (o.isNull(key)) null else o.optString(key)
private fun optInt(o: JSONObject, key: String): Int? = if (o.isNull(key)) null else o.optInt(key)

fun exportBundleToJson(b: ExportBundle): String {
    val root = JSONObject()

    root.put("moods", JSONArray().apply {
        b.moods.forEach { m ->
            put(JSONObject().put("id", m.id).put("date", m.date).put("value", m.value).put("note", m.note))
        }
    })

    root.put("thoughts", JSONArray().apply {
        b.thoughts.forEach { t ->
            put(
                JSONObject()
                    .put("id", t.id).put("date", t.date).put("situation", t.situation)
                    .put("emotion", t.emotion).put("intensityBefore", t.intensityBefore)
                    .put("thought", t.thought).put("distortionIds", t.distortionIds)
                    .put("forEv", t.forEv).put("againstEv", t.againstEv)
                    .put("balanced", t.balanced).put("intensityAfter", t.intensityAfter)
            )
        }
    })

    root.put("downward", JSONArray().apply {
        b.downward.forEach { d ->
            put(JSONObject().put("id", d.id).put("date", d.date).put("chainJson", d.chainJson).put("core", d.core))
        }
    })

    root.put("activities", JSONArray().apply {
        b.activities.forEach { a ->
            put(
                JSONObject()
                    .put("id", a.id).put("date", a.date).put("activity", a.activity).put("category", a.category)
                    .put("expPleasure", a.expPleasure).put("expMastery", a.expMastery).put("done", a.done)
                    .put("actPleasure", a.actPleasure ?: JSONObject.NULL)
                    .put("actMastery", a.actMastery ?: JSONObject.NULL)
            )
        }
    })

    root.put("experiments", JSONArray().apply {
        b.experiments.forEach { x ->
            put(
                JSONObject()
                    .put("id", x.id).put("date", x.date).put("belief", x.belief).put("confBefore", x.confBefore)
                    .put("plan", x.plan).put("prediction", x.prediction)
                    .put("outcome", x.outcome ?: JSONObject.NULL)
                    .put("confAfter", x.confAfter ?: JSONObject.NULL)
                    .put("learned", x.learned ?: JSONObject.NULL)
            )
        }
    })

    root.put("exposures", JSONArray().apply {
        b.exposures.forEach { x ->
            put(
                JSONObject()
                    .put("id", x.id).put("date", x.date).put("situation", x.situation).put("anxiety", x.anxiety)
                    .put("completed", x.completed)
                    .put("actualAnxiety", x.actualAnxiety ?: JSONObject.NULL)
            )
        }
    })

    root.put("problems", JSONArray().apply {
        b.problems.forEach { p ->
            put(
                JSONObject()
                    .put("id", p.id).put("date", p.date).put("problem", p.problem).put("solutions", p.solutions)
                    .put("prosCons", p.prosCons).put("chosen", p.chosen).put("plan", p.plan).put("outcome", p.outcome)
            )
        }
    })

    root.put("worries", JSONArray().apply {
        b.worries.forEach { w ->
            put(JSONObject().put("id", w.id).put("date", w.date).put("text", w.text).put("resolved", w.resolved))
        }
    })

    root.put("worryTime", b.worryTime)

    if (b.relapsePlan != null) {
        root.put(
            "relapsePlan",
            JSONObject()
                .put("warning", b.relapsePlan.warning).put("triggers", b.relapsePlan.triggers)
                .put("strategies", b.relapsePlan.strategies).put("support", b.relapsePlan.support)
                .put("firstStep", b.relapsePlan.firstStep)
        )
    } else {
        root.put("relapsePlan", JSONObject.NULL)
    }

    return root.toString(2)
}

fun parseImportJson(json: String): ExportBundle {
    val root = JSONObject(json)

    val moods = mutableListOf<MoodEntry>()
    root.optJSONArray("moods")?.let { arr ->
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            moods.add(MoodEntry(o.getString("id"), o.getLong("date"), o.getInt("value"), o.optString("note")))
        }
    }

    val thoughts = mutableListOf<ThoughtRecord>()
    root.optJSONArray("thoughts")?.let { arr ->
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            thoughts.add(
                ThoughtRecord(
                    o.getString("id"), o.getLong("date"), o.optString("situation"), o.optString("emotion"),
                    o.optInt("intensityBefore"), o.optString("thought"), o.optString("distortionIds"),
                    o.optString("forEv"), o.optString("againstEv"), o.optString("balanced"), o.optInt("intensityAfter")
                )
            )
        }
    }

    val downward = mutableListOf<DownwardChain>()
    root.optJSONArray("downward")?.let { arr ->
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            downward.add(DownwardChain(o.getString("id"), o.getLong("date"), o.optString("chainJson"), o.optString("core")))
        }
    }

    val activities = mutableListOf<ActivityEntry>()
    root.optJSONArray("activities")?.let { arr ->
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            activities.add(
                ActivityEntry(
                    o.getString("id"), o.getLong("date"), o.optString("activity"), o.optString("category"),
                    o.optInt("expPleasure"), o.optInt("expMastery"), o.optBoolean("done"),
                    optInt(o, "actPleasure"), optInt(o, "actMastery")
                )
            )
        }
    }

    val experiments = mutableListOf<ExperimentEntry>()
    root.optJSONArray("experiments")?.let { arr ->
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            experiments.add(
                ExperimentEntry(
                    o.getString("id"), o.getLong("date"), o.optString("belief"), o.optInt("confBefore"),
                    o.optString("plan"), o.optString("prediction"),
                    opt(o, "outcome"), optInt(o, "confAfter"), opt(o, "learned")
                )
            )
        }
    }

    val exposures = mutableListOf<ExposureEntry>()
    root.optJSONArray("exposures")?.let { arr ->
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            exposures.add(
                ExposureEntry(
                    o.getString("id"), o.getLong("date"), o.optString("situation"), o.optInt("anxiety"),
                    o.optBoolean("completed"), optInt(o, "actualAnxiety")
                )
            )
        }
    }

    val problems = mutableListOf<ProblemEntry>()
    root.optJSONArray("problems")?.let { arr ->
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            problems.add(
                ProblemEntry(
                    o.getString("id"), o.getLong("date"), o.optString("problem"), o.optString("solutions"),
                    o.optString("prosCons"), o.optString("chosen"), o.optString("plan"), o.optString("outcome")
                )
            )
        }
    }

    val worries = mutableListOf<WorryEntry>()
    root.optJSONArray("worries")?.let { arr ->
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            worries.add(WorryEntry(o.getString("id"), o.getLong("date"), o.optString("text"), o.optBoolean("resolved")))
        }
    }

    val worryTime = root.optString("worryTime", "")

    val relapsePlan = if (!root.isNull("relapsePlan")) {
        val o = root.getJSONObject("relapsePlan")
        RelapsePlanEntity(
            1, o.optString("warning"), o.optString("triggers"), o.optString("strategies"),
            o.optString("support"), o.optString("firstStep")
        )
    } else null

    return ExportBundle(moods, thoughts, downward, activities, experiments, exposures, problems, worries, worryTime, relapsePlan)
}
