package com.rafiq.cbt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.cbt.AppViewModel
import com.rafiq.cbt.data.decodeChain
import com.rafiq.cbt.data.distortions
import com.rafiq.cbt.data.toolHelp
import com.rafiq.cbt.ui.components.*
import com.rafiq.cbt.ui.theme.RustColor

@Composable
fun ThoughtsScreen(vm: AppViewModel, sub: String, onSubChange: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp, 16.dp)) {
        SubTabRow(
            items = listOf("record" to "سجل الأفكار", "distortions" to "التشوهات الفكرية", "downward" to "السهم الهابط"),
            selected = sub, onSelect = onSubChange
        )
        when (sub) {
            "record" -> ThoughtRecordSection(vm)
            "distortions" -> DistortionsSection()
            "downward" -> DownwardArrowSection(vm)
        }
    }
}

@Composable
private fun ThoughtRecordSection(vm: AppViewModel) {
    val thoughts by vm.thoughts.collectAsState()
    var situation by remember { mutableStateOf("") }
    var emotion by remember { mutableStateOf("") }
    var intensityBefore by remember { mutableStateOf(50) }
    var thought by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(setOf<String>()) }
    var forEv by remember { mutableStateOf("") }
    var againstEv by remember { mutableStateOf("") }
    var balanced by remember { mutableStateOf("") }
    var intensityAfter by remember { mutableStateOf(50) }

    LazyColumn(Modifier.fillMaxSize()) {
        item { TitleWithHelp("سجل الأفكار", toolHelp["record"] ?: "") }
        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                LabeledField("الموقف — شنو صار بالضبط؟", situation, { situation = it }, "مثال: تأخرت بالرد على رسالة صديقي")
                LabeledField("الشعور", emotion, { emotion = it }, "مثال: قلق، خجل", singleLine = true)
                LabeledSlider("شدة الشعور الآن", intensityBefore, 0f..100f, { intensityBefore = it })
                LabeledField("الفكرة التلقائية", thought, { thought = it }, "شنو الفكرة اللي خطرت ببالك فورًا؟")
                FieldLabel("هل توجد تشوهات فكرية بهالفكرة؟")
                MultiChipRow(distortions.map { it.id to it.name }, selected) { id ->
                    selected = if (selected.contains(id)) selected - id else selected + id
                }
                Spacer(Modifier.height(6.dp))
                LabeledField("الدليل المؤيد للفكرة", forEv, { forEv = it }, "شنو يدعم صحة هالفكرة فعلاً؟")
                LabeledField("الدليل المعارض للفكرة", againstEv, { againstEv = it }, "شنو يناقض هالفكرة أو يقدّم زاوية ثانية؟")
                LabeledField("فكرة بديلة أكثر توازنًا", balanced, { balanced = it }, "بالاستناد للدليلين، شلون تكتب الفكرة بصورة أعدل؟")
                LabeledSlider("شدة الشعور بعد إعادة الصياغة", intensityAfter, 0f..100f, { intensityAfter = it })
                PrimaryButton("حفظ سجل الفكرة", onClick = {
                    if (situation.isNotBlank() && thought.isNotBlank()) {
                        vm.addThought(situation.trim(), emotion.trim(), intensityBefore, thought.trim(), selected.toList(), forEv.trim(), againstEv.trim(), balanced.trim(), intensityAfter)
                        situation = ""; emotion = ""; thought = ""; forEv = ""; againstEv = ""; balanced = ""
                        selected = setOf(); intensityBefore = 50; intensityAfter = 50
                    }
                })
            }
        }
        item {
            SectionCard {
                SectionTitle("السجلات السابقة")
                if (thoughts.isEmpty()) {
                    EmptyState("لسع ماكو سجلات — عبّي النموذج فوق أول مرة تنتبه لفكرة مزعجة.")
                } else {
                    thoughts.forEach { t ->
                        val tags = t.distortionIds.split(",").filter { it.isNotBlank() }.mapNotNull { id -> distortions.find { it.id == id }?.name }
                        EntryRow(
                            meta = "${formatDate(t.date)}${if (t.emotion.isNotBlank()) " — ${t.emotion}" else ""} (${t.intensityBefore} ← ${t.intensityAfter})",
                            body = "الموقف: ${t.situation}\nالفكرة: ${t.thought}" + (if (t.balanced.isNotBlank()) "\nالفكرة المتوازنة: ${t.balanced}" else ""),
                            extra = {
                                if (tags.isNotEmpty()) {
                                    androidx.compose.material3.Text("التشوهات: ${tags.joinToString("، ")}", color = RustColor, fontSize = 12.sp)
                                }
                            },
                            onDelete = { vm.deleteThought(t.id) }
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun DistortionsSection() {
    var openId by remember { mutableStateOf<String?>(null) }
    LazyColumn(Modifier.fillMaxSize()) {
        item { TitleWithHelp("التشوهات الفكرية", toolHelp["distortions"] ?: "") }
        item {
            SectionCard {
                distortions.forEach { d ->
                    AccordionItem(d.name, openId == d.id, { openId = if (openId == d.id) null else d.id }) {
                        MutedText(d.desc)
                        ExampleBox(d.example)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun DownwardArrowSection(vm: AppViewModel) {
    val chains by vm.downward.collectAsState()
    var tempChain by remember { mutableStateOf(listOf<String>()) }
    var input by remember { mutableStateOf("") }
    var openId by remember { mutableStateOf<String?>(null) }

    LazyColumn(Modifier.fillMaxSize()) {
        item { TitleWithHelp("السهم الهابط", toolHelp["downward"] ?: "") }
        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                FieldLabel("سلسلة الأفكار الحالية")
                tempChain.forEachIndexed { i, c ->
                    ChainStepBox(c)
                    ChainArrow()
                }
                LabeledField("", input, { input = it }, if (tempChain.isEmpty()) "اكتب الفكرة أو الإجابة هنا" else "وإذا كان هذا صحيحًا، شنو يعني هذا إلي؟")
                SecondaryButton("أضف خطوة ↓", {
                    if (input.isNotBlank()) { tempChain = tempChain + input.trim(); input = "" }
                }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                PrimaryButton("هذي آخر خطوة — احفظ كمعتقد جوهري", onClick = {
                    val full = if (input.isNotBlank()) tempChain + input.trim() else tempChain
                    if (full.isNotEmpty()) {
                        vm.addDownwardChain(full)
                        tempChain = listOf(); input = ""
                    }
                })
            }
        }
        item {
            SectionCard {
                SectionTitle("السلاسل المحفوظة")
                if (chains.isEmpty()) {
                    EmptyState("لسع ماكو سلاسل محفوظة.")
                } else {
                    chains.forEach { dw ->
                        val steps = decodeChain(dw.chainJson)
                        AccordionItem("${formatDate(dw.date)} — ${dw.core.take(35)}", openId == dw.id, { openId = if (openId == dw.id) null else dw.id }) {
                            steps.dropLast(1).forEach { s -> ChainStepBox(s); ChainArrow() }
                            ChainCoreBox(dw.core)
                            Spacer(Modifier.height(10.dp))
                            DangerButton("حذف السلسلة", { vm.deleteDownward(dw.id) })
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}
