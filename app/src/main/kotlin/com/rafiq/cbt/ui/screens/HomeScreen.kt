package com.rafiq.cbt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rafiq.cbt.AppViewModel
import com.rafiq.cbt.ui.components.*
import com.rafiq.cbt.ui.theme.AmberColor
import com.rafiq.cbt.ui.theme.GreenColor
import com.rafiq.cbt.ui.theme.RustColor

private data class HomeLink(val icon: String, val label: String, val tab: String, val sub: String?)

private val homeLinks = listOf(
    HomeLink("🌙", "تتبّع المزاج", "mood", null),
    HomeLink("💭", "سجل الأفكار", "thoughts", "record"),
    HomeLink("🧠", "التشوهات الفكرية", "thoughts", "distortions"),
    HomeLink("⬇️", "السهم الهابط", "thoughts", "downward"),
    HomeLink("📅", "التنشيط السلوكي", "tools", "activation"),
    HomeLink("🧪", "تجارب سلوكية", "tools", "experiments"),
    HomeLink("🪜", "سلّم التعرّض", "tools", "exposure"),
    HomeLink("🧩", "حل المشكلات", "tools", "problem"),
    HomeLink("⏰", "وقت القلق", "tools", "worry"),
    HomeLink("🛡️", "خطة الوقاية", "tools", "relapse"),
    HomeLink("🌬️", "تمرين التنفس", "relax", "breath"),
    HomeLink("🌳", "التأريض 5-4-3-2-1", "relax", "ground"),
    HomeLink("💆", "استرخاء عضلي", "relax", "pmr"),
    HomeLink("📈", "المراجعة الأسبوعية", "review", null),
    HomeLink("🔍", "البحث بالسجلات", "search", null)
)

@Composable
fun HomeScreen(vm: AppViewModel, onNavigate: (String, String?) -> Unit) {
    val moods by vm.moods.collectAsState()
    val thoughts by vm.thoughts.collectAsState()
    val activities by vm.activities.collectAsState()
    val exposures by vm.exposures.collectAsState()

    val last7 = moods.takeLast(7)
    val avg = if (last7.isNotEmpty()) String.format("%.1f", last7.map { it.value }.average()) else "—"
    val linkRows = homeLinks.chunked(2)

    LazyColumn(Modifier.fillMaxSize().padding(18.dp, 16.dp)) {
        item { SectionTitle("نظرة سريعة") }
        item {
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                StatCard(avg, "متوسط المزاج (٧ أيام)", GreenColor, Modifier.weight(1f).padding(end = 5.dp))
                StatCard("${thoughts.size}", "سجلات أفكار", AmberColor, Modifier.weight(1f).padding(start = 5.dp))
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(bottom = 20.dp)) {
                StatCard("${activities.count { it.done }}", "أنشطة منجزة", AmberColor, Modifier.weight(1f).padding(end = 5.dp))
                StatCard("${exposures.count { it.completed }}", "خطوات تعرّض مكتملة", RustColor, Modifier.weight(1f).padding(start = 5.dp))
            }
        }
        item { SectionTitle("جميع الأدوات") }
        items(linkRows) { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { link ->
                    ToolLink(link.icon, link.label, Modifier.weight(1f)) { onNavigate(link.tab, link.sub) }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item { Spacer(Modifier.height(10.dp)) }
    }
}
