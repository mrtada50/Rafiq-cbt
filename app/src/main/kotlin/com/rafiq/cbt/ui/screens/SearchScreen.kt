package com.rafiq.cbt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.cbt.AppViewModel
import com.rafiq.cbt.data.toolHelp
import com.rafiq.cbt.ui.components.*
import com.rafiq.cbt.ui.theme.AmberColor

private data class SearchResult(val type: String, val date: Long, val snippet: String)

@Composable
fun SearchScreen(vm: AppViewModel) {
    val moods by vm.moods.collectAsState()
    val thoughts by vm.thoughts.collectAsState()
    val downward by vm.downward.collectAsState()
    val activities by vm.activities.collectAsState()
    val experiments by vm.experiments.collectAsState()
    val exposures by vm.exposures.collectAsState()
    val problems by vm.problems.collectAsState()
    val worries by vm.worries.collectAsState()

    var query by remember { mutableStateOf("") }

    val results = remember(query, moods, thoughts, downward, activities, experiments, exposures, problems, worries) {
        if (query.isBlank()) emptyList() else {
            val q = query.trim()
            val list = mutableListOf<SearchResult>()
            moods.forEach { if (it.note.contains(q, true)) list.add(SearchResult("المزاج", it.date, it.note)) }
            thoughts.forEach {
                val combined = "${it.situation} ${it.thought} ${it.balanced} ${it.forEv} ${it.againstEv}"
                if (combined.contains(q, true)) list.add(SearchResult("سجل أفكار", it.date, it.thought))
            }
            downward.forEach { if (it.core.contains(q, true) || it.chainJson.contains(q, true)) list.add(SearchResult("السهم الهابط", it.date, it.core)) }
            activities.forEach { if (it.activity.contains(q, true)) list.add(SearchResult("التنشيط السلوكي", it.date, it.activity)) }
            experiments.forEach {
                val combined = "${it.belief} ${it.plan} ${it.prediction} ${it.outcome ?: ""}"
                if (combined.contains(q, true)) list.add(SearchResult("تجارب سلوكية", it.date, it.belief))
            }
            exposures.forEach { if (it.situation.contains(q, true)) list.add(SearchResult("سلّم التعرّض", it.date, it.situation)) }
            problems.forEach {
                val combined = "${it.problem} ${it.solutions} ${it.chosen} ${it.plan} ${it.outcome}"
                if (combined.contains(q, true)) list.add(SearchResult("حل المشكلات", it.date, it.problem))
            }
            worries.forEach { if (it.text.contains(q, true)) list.add(SearchResult("وقت القلق", it.date, it.text)) }
            list.sortedByDescending { it.date }
        }
    }

    LazyColumn(Modifier.fillMaxSize().padding(18.dp, 16.dp)) {
        item { TitleWithHelp("البحث", toolHelp["search"] ?: "") }
        item {
            LabeledField("", query, { query = it }, "اكتب كلمة تدوّر عليها بكل سجلاتك...", singleLine = true)
        }
        if (query.isNotBlank()) {
            if (results.isEmpty()) {
                item { EmptyState("ماكو نتائج مطابقة.") }
            } else {
                items(results) { r ->
                    SectionCard(Modifier.padding(bottom = 10.dp)) {
                        Text(r.type, color = AmberColor, fontSize = 11.5.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(formatDate(r.date), color = com.rafiq.cbt.ui.theme.TextMuted, fontSize = 11.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(r.snippet, color = com.rafiq.cbt.ui.theme.TextColor, fontSize = 13.5.sp)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}
