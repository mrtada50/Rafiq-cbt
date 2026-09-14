package com.rafiq.cbt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.cbt.AppViewModel
import com.rafiq.cbt.data.distortions
import com.rafiq.cbt.data.toolHelp
import com.rafiq.cbt.ui.components.*
import com.rafiq.cbt.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private fun dayKey(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date(millis))

@Composable
fun ReviewScreen(vm: AppViewModel) {
    val moods by vm.moods.collectAsState()
    val thoughts by vm.thoughts.collectAsState()
    val activities by vm.activities.collectAsState()

    var period by remember { mutableStateOf("week") }

    val rangeStart = remember(period) {
        val cal = Calendar.getInstance()
        if (period == "week") cal.add(Calendar.DAY_OF_YEAR, -7) else cal.add(Calendar.DAY_OF_YEAR, -30)
        cal.timeInMillis
    }

    val periodMoods = moods.filter { it.date >= rangeStart }
    val periodThoughts = thoughts.filter { it.date >= rangeStart }
    val periodActivities = activities.filter { it.date >= rangeStart }

    val avgMood = if (periodMoods.isNotEmpty()) String.format("%.1f", periodMoods.map { it.value }.average()) else "—"

    val distortionCounts = periodThoughts
        .flatMap { it.distortionIds.split(",").filter { s -> s.isNotBlank() } }
        .groupingBy { it }.eachCount()
    val topDistortion = distortionCounts.maxByOrNull { it.value }
        ?.let { entry -> distortions.find { it.id == entry.key }?.name }

    val doneActivityDays = periodActivities.filter { it.done }.map { dayKey(it.date) }.toSet()
    val moodsOnActiveDays = periodMoods.filter { dayKey(it.date) in doneActivityDays }
    val moodsOnRestDays = periodMoods.filter { dayKey(it.date) !in doneActivityDays }
    val avgOnActive = if (moodsOnActiveDays.isNotEmpty()) String.format("%.1f", moodsOnActiveDays.map { it.value }.average()) else null
    val avgOnRest = if (moodsOnRestDays.isNotEmpty()) String.format("%.1f", moodsOnRestDays.map { it.value }.average()) else null

    LazyColumn(Modifier.fillMaxSize().padding(18.dp, 16.dp)) {
        item { TitleWithHelp("المراجعة", toolHelp["review"] ?: "") }
        item {
            SubTabRow(
                items = listOf("week" to "آخر ٧ أيام", "month" to "آخر ٣٠ يوم"),
                selected = period, onSelect = { period = it }
            )
        }
        item {
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                StatCard(avgMood, "متوسط المزاج", GreenColor, Modifier.weight(1f).padding(end = 5.dp))
                StatCard("${periodThoughts.size}", "سجلات أفكار", AmberColor, Modifier.weight(1f).padding(start = 5.dp))
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                StatCard("${periodActivities.count { it.done }}", "أنشطة منجزة", AmberColor, Modifier.weight(1f).padding(end = 5.dp))
                StatCard("${periodMoods.size}", "تسجيلات مزاج", RustColor, Modifier.weight(1f).padding(start = 5.dp))
            }
        }
        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                SectionTitle("اتجاه المزاج")
                if (periodMoods.isEmpty()) {
                    EmptyState("ماكو تسجيلات مزاج بهالفترة بعد.")
                } else {
                    Row(Modifier.fillMaxWidth().height(70.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        periodMoods.takeLast(30).forEach { m ->
                            val h = (m.value / 10f * 70).dp.coerceAtLeast(4.dp)
                            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                                Box(Modifier.fillMaxWidth().height(h).background(AmberDim, RoundedCornerShape(2.dp, 2.dp, 0.dp, 0.dp)))
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                SectionTitle("التشوه الفكري الأكثر تكرارًا")
                if (topDistortion != null) {
                    Text(topDistortion, color = RustColor, fontSize = 14.sp)
                } else {
                    EmptyState("ماكو تشوهات مسجّلة بهالفترة بعد.")
                }
            }
        }
        item {
            SectionCard {
                SectionTitle("المزاج والنشاط")
                if (avgOnActive != null || avgOnRest != null) {
                    MutedText("متوسط مزاجك بالأيام اللي أنجزت فيها نشاط: ${avgOnActive ?: "—"}")
                    Spacer(Modifier.height(4.dp))
                    MutedText("متوسط مزاجك بالأيام الثانية: ${avgOnRest ?: "—"}")
                } else {
                    EmptyState("نحتاج بيانات مزاج وأنشطة أكثر لعرض المقارنة.")
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}
