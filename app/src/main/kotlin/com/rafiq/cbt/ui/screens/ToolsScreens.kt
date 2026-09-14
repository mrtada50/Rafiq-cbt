package com.rafiq.cbt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.cbt.AppViewModel
import com.rafiq.cbt.data.ActivityEntry
import com.rafiq.cbt.data.ExperimentEntry
import com.rafiq.cbt.data.ExposureEntry
import com.rafiq.cbt.data.toolHelp
import com.rafiq.cbt.ui.components.*
import com.rafiq.cbt.ui.theme.GreenColor
import com.rafiq.cbt.ui.theme.TextColor
import com.rafiq.cbt.ui.theme.TextMuted

@Composable
fun ToolsScreen(vm: AppViewModel, sub: String, onSubChange: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp, 16.dp)) {
        SubTabRow(
            items = listOf(
                "activation" to "التنشيط السلوكي", "experiments" to "تجارب سلوكية", "exposure" to "سلّم التعرّض",
                "problem" to "حل المشكلات", "worry" to "وقت القلق", "relapse" to "خطة الوقاية"
            ),
            selected = sub, onSelect = onSubChange
        )
        when (sub) {
            "activation" -> ActivationSection(vm)
            "experiments" -> ExperimentsSection(vm)
            "exposure" -> ExposureSection(vm)
            "problem" -> ProblemSection(vm)
            "worry" -> WorrySection(vm)
            "relapse" -> RelapseSection(vm)
        }
    }
}

/* ---------------- Behavioral Activation ---------------- */
private val categories = listOf("ضروري", "ممتع", "اجتماعي", "بدني")

@Composable
private fun ActivationSection(vm: AppViewModel) {
    val activities by vm.activities.collectAsState()
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ضروري") }
    var expPleasure by remember { mutableStateOf(5) }
    var expMastery by remember { mutableStateOf(5) }
    var resultDrafts by remember { mutableStateOf(mapOf<String, Pair<Int, Int>>()) }
    var revealed by remember { mutableStateOf(setOf<String>()) }

    LazyColumn(Modifier.fillMaxSize()) {
        item { TitleWithHelp("التنشيط السلوكي", toolHelp["activation"] ?: "") }
        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                LabeledField("النشاط", name, { name = it }, "مثال: مشي 15 دقيقة، اتصال بصديق", singleLine = true)
                FieldLabel("الفئة")
                SingleChipRow(categories, category) { category = it }
                Spacer(Modifier.height(4.dp))
                LabeledSlider("المتعة المتوقعة", expPleasure, 0f..10f, { expPleasure = it })
                LabeledSlider("الإنجاز المتوقع", expMastery, 0f..10f, { expMastery = it })
                PrimaryButton("أضف للخطة", onClick = {
                    if (name.isNotBlank()) { vm.addActivity(name.trim(), category, expPleasure, expMastery); name = "" }
                })
            }
        }
        item {
            SectionCard {
                SectionTitle("أنشطتك")
                if (activities.isEmpty()) EmptyState("لسع ماكو أنشطة مجدولة.")
                activities.forEach { a ->
                    ActivityRow(a, revealed.contains(a.id),
                        onReveal = { revealed = revealed + a.id },
                        draft = resultDrafts[a.id] ?: (5 to 5),
                        onDraftChange = { p -> resultDrafts = resultDrafts + (a.id to p) },
                        onSave = { p -> vm.completeActivity(a, p.first, p.second); revealed = revealed - a.id },
                        onDelete = { vm.deleteActivity(a.id) }
                    )
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun ActivityRow(a: ActivityEntry, isRevealed: Boolean, onReveal: () -> Unit, draft: Pair<Int, Int>, onDraftChange: (Pair<Int, Int>) -> Unit, onSave: (Pair<Int, Int>) -> Unit, onDelete: () -> Unit) {
    EntryRow(
        meta = "${formatDate(a.date)} — ${a.category}",
        body = "${a.activity}\nمتوقع: متعة ${a.expPleasure}/10، إنجاز ${a.expMastery}/10",
        extra = {
            if (a.done) {
                Text("فعلي: متعة ${a.actPleasure}/10، إنجاز ${a.actMastery}/10 ✓", color = GreenColor, fontSize = 12.sp)
            } else if (isRevealed) {
                Spacer(Modifier.height(8.dp))
                LabeledSlider("المتعة الفعلية", draft.first, 0f..10f, { onDraftChange(it to draft.second) })
                LabeledSlider("الإنجاز الفعلي", draft.second, 0f..10f, { onDraftChange(draft.first to it) })
                SecondaryButton("حفظ النتيجة", { onSave(draft) })
            } else {
                Spacer(Modifier.height(8.dp))
                SecondaryButton("تسجيل النتيجة بعد التنفيذ", onReveal)
            }
        },
        onDelete = onDelete
    )
}

/* ---------------- Behavioral Experiments ---------------- */
@Composable
private fun ExperimentsSection(vm: AppViewModel) {
    val experiments by vm.experiments.collectAsState()
    var belief by remember { mutableStateOf("") }
    var confBefore by remember { mutableStateOf(50) }
    var plan by remember { mutableStateOf("") }
    var prediction by remember { mutableStateOf("") }
    var revealed by remember { mutableStateOf(setOf<String>()) }
    var outcomeDrafts by remember { mutableStateOf(mapOf<String, String>()) }
    var learnedDrafts by remember { mutableStateOf(mapOf<String, String>()) }
    var confAfterDrafts by remember { mutableStateOf(mapOf<String, Int>()) }

    LazyColumn(Modifier.fillMaxSize()) {
        item { TitleWithHelp("تجارب سلوكية", toolHelp["experiments"] ?: "") }
        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                LabeledField("الفكرة أو الافتراض المراد اختباره", belief, { belief = it }, "مثال: إذا اعترضت برأيي بالاجتماع راح يسخرون مني")
                LabeledSlider("مستوى ثقتك بهالفكرة الآن", confBefore, 0f..100f, { confBefore = it })
                LabeledField("التجربة المخطط لها (شنو، وين، متى)", plan, { plan = it }, "مثال: راح أطرح رأيي بالاجتماع القادم")
                LabeledField("التنبؤ — شنو تتوقع صايره؟", prediction, { prediction = it }, "مثال: راح يسكتون ويشوفون فيّة بنظرة غريبة")
                PrimaryButton("حفظ التجربة", onClick = {
                    if (belief.isNotBlank()) {
                        vm.addExperiment(belief.trim(), confBefore, plan.trim(), prediction.trim())
                        belief = ""; plan = ""; prediction = ""
                    }
                })
            }
        }
        item {
            SectionCard {
                SectionTitle("تجاربك")
                if (experiments.isEmpty()) EmptyState("لسع ماكو تجارب محفوظة.")
                experiments.forEach { x ->
                    ExperimentRow(
                        x, revealed.contains(x.id), onReveal = { revealed = revealed + x.id },
                        outcome = outcomeDrafts[x.id] ?: "", onOutcomeChange = { outcomeDrafts = outcomeDrafts + (x.id to it) },
                        learned = learnedDrafts[x.id] ?: "", onLearnedChange = { learnedDrafts = learnedDrafts + (x.id to it) },
                        confAfter = confAfterDrafts[x.id] ?: 50, onConfAfterChange = { confAfterDrafts = confAfterDrafts + (x.id to it) },
                        onSave = {
                            vm.completeExperiment(x, outcomeDrafts[x.id] ?: "", confAfterDrafts[x.id] ?: 50, learnedDrafts[x.id] ?: "")
                            revealed = revealed - x.id
                        },
                        onDelete = { vm.deleteExperiment(x.id) }
                    )
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun ExperimentRow(
    x: ExperimentEntry, isRevealed: Boolean, onReveal: () -> Unit,
    outcome: String, onOutcomeChange: (String) -> Unit,
    learned: String, onLearnedChange: (String) -> Unit,
    confAfter: Int, onConfAfterChange: (Int) -> Unit,
    onSave: () -> Unit, onDelete: () -> Unit
) {
    EntryRow(
        meta = "${formatDate(x.date)} — ثقة قبل: ${x.confBefore}%",
        body = "الفكرة: ${x.belief}" + (if (x.plan.isNotBlank()) "\nالخطة: ${x.plan}" else "") + (if (x.prediction.isNotBlank()) "\nالتنبؤ: ${x.prediction}" else ""),
        extra = {
            if (x.outcome != null) {
                Text("النتيجة الفعلية: ${x.outcome}", color = GreenColor, fontSize = 12.5.sp)
                Text("ثقة بعد: ${x.confAfter}%" + (if (!x.learned.isNullOrBlank()) " — تعلمت: ${x.learned}" else ""), color = TextMuted, fontSize = 11.5.sp)
            } else if (isRevealed) {
                Spacer(Modifier.height(8.dp))
                LabeledField("شنو صار فعليًا؟", outcome, onOutcomeChange)
                LabeledSlider("ثقتك بالفكرة الآن", confAfter, 0f..100f, onConfAfterChange)
                LabeledField("شنو تعلمت من هالتجربة؟", learned, onLearnedChange)
                SecondaryButton("حفظ النتيجة", onSave)
            } else {
                Spacer(Modifier.height(8.dp))
                SecondaryButton("تسجيل النتيجة", onReveal)
            }
        },
        onDelete = onDelete
    )
}

/* ---------------- Exposure Ladder ---------------- */
@Composable
private fun ExposureSection(vm: AppViewModel) {
    val exposures by vm.exposures.collectAsState()
    var situation by remember { mutableStateOf("") }
    var anxiety by remember { mutableStateOf(50) }
    var revealed by remember { mutableStateOf(setOf<String>()) }
    var actualDrafts by remember { mutableStateOf(mapOf<String, Int>()) }

    LazyColumn(Modifier.fillMaxSize()) {
        item { TitleWithHelp("سلّم التعرّض", toolHelp["exposure"] ?: "") }
        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                LabeledField("الموقف المثير للقلق", situation, { situation = it }, "مثال: التحدث أمام مجموعة صغيرة")
                LabeledSlider("مستوى القلق المتوقع", anxiety, 0f..100f, { anxiety = it })
                PrimaryButton("أضف للسلّم", onClick = {
                    if (situation.isNotBlank()) { vm.addExposure(situation.trim(), anxiety); situation = "" }
                })
            }
        }
        item {
            SectionCard {
                SectionTitle("سلّمك (من الأسهل للأصعب)")
                if (exposures.isEmpty()) EmptyState("لسع ماكو مواقف مضافة للسلّم.")
                exposures.sortedBy { it.anxiety }.forEach { x ->
                    ExposureRow(
                        x, revealed.contains(x.id), onReveal = { revealed = revealed + x.id },
                        actual = actualDrafts[x.id] ?: 50, onActualChange = { actualDrafts = actualDrafts + (x.id to it) },
                        onSave = { vm.completeExposure(x, actualDrafts[x.id] ?: 50); revealed = revealed - x.id },
                        onDelete = { vm.deleteExposure(x.id) }
                    )
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
private fun ExposureRow(x: ExposureEntry, isRevealed: Boolean, onReveal: () -> Unit, actual: Int, onActualChange: (Int) -> Unit, onSave: () -> Unit, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        CenteredNumberBadge("${x.anxiety}")
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(x.situation, color = TextColor, fontSize = 13.5.sp)
            if (x.completed) {
                Text("القلق الفعلي بعد التعرض: ${x.actualAnxiety}/100 ✓", color = TextMuted, fontSize = 11.5.sp)
            } else if (isRevealed) {
                Spacer(Modifier.height(6.dp))
                LabeledSlider("", actual, 0f..100f, onActualChange)
                SecondaryButton("تأكيد الإنجاز", onSave)
            } else {
                Spacer(Modifier.height(6.dp))
                SecondaryButton("تم التعرض ✓", onReveal)
            }
        }
        IconButtonX(onDelete)
    }
}

/* ---------------- Problem Solving ---------------- */
@Composable
private fun ProblemSection(vm: AppViewModel) {
    val problems by vm.problems.collectAsState()
    var problem by remember { mutableStateOf("") }
    var solutions by remember { mutableStateOf("") }
    var prosCons by remember { mutableStateOf("") }
    var chosen by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf("") }
    var outcome by remember { mutableStateOf("") }
    var openId by remember { mutableStateOf<String?>(null) }

    LazyColumn(Modifier.fillMaxSize()) {
        item { TitleWithHelp("حل المشكلات", toolHelp["problem"] ?: "") }
        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                LabeledField("وصف المشكلة بدقّة", problem, { problem = it }, "شنو بالضبط المشكلة اللي تواجهك؟")
                LabeledField("الحلول الممكنة (كل حل بسطر)", solutions, { solutions = it }, "1- ...\n2- ...", minLines = 3)
                LabeledField("إيجابيات وسلبيات أهم الحلول", prosCons, { prosCons = it })
                LabeledField("الحل المختار", chosen, { chosen = it }, singleLine = true)
                LabeledField("خطوات التنفيذ (شنو، متى، شلون)", plan, { plan = it })
                LabeledField("تقييم النتيجة (تقدر تعبيها بعد التجربة)", outcome, { outcome = it }, "اختياري الآن")
                PrimaryButton("حفظ الجلسة", onClick = {
                    if (problem.isNotBlank()) {
                        vm.addProblem(problem.trim(), solutions.trim(), prosCons.trim(), chosen.trim(), plan.trim(), outcome.trim())
                        problem = ""; solutions = ""; prosCons = ""; chosen = ""; plan = ""; outcome = ""
                    }
                })
            }
        }
        item {
            SectionCard {
                SectionTitle("جلساتك السابقة")
                if (problems.isEmpty()) EmptyState("لسع ماكو جلسات محفوظة.")
                problems.forEach { p ->
                    AccordionItem("${formatDate(p.date)} — ${p.problem.take(35)}", openId == p.id, { openId = if (openId == p.id) null else p.id }) {
                        Text("المشكلة: ${p.problem}", color = TextMuted, fontSize = 13.sp)
                        if (p.solutions.isNotBlank()) Text("\nالحلول:\n${p.solutions}", color = TextMuted, fontSize = 13.sp)
                        if (p.prosCons.isNotBlank()) Text("\nالإيجابيات والسلبيات:\n${p.prosCons}", color = TextMuted, fontSize = 13.sp)
                        if (p.chosen.isNotBlank()) Text("\nالحل المختار: ${p.chosen}", color = TextMuted, fontSize = 13.sp)
                        if (p.plan.isNotBlank()) Text("\nخطوات التنفيذ:\n${p.plan}", color = TextMuted, fontSize = 13.sp)
                        if (p.outcome.isNotBlank()) Text("\nالنتيجة: ${p.outcome}", color = GreenColor, fontSize = 13.sp)
                        Spacer(Modifier.height(10.dp))
                        DangerButton("حذف السجل", { vm.deleteProblem(p.id) })
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

/* ---------------- Worry Time ---------------- */
@Composable
private fun WorrySection(vm: AppViewModel) {
    val worries by vm.worries.collectAsState()
    val savedTime by vm.worryTimeSetting.collectAsState()
    var timeInput by remember { mutableStateOf("") }
    var worryText by remember { mutableStateOf("") }

    LaunchedEffect(savedTime) { timeInput = savedTime?.value ?: "" }

    LazyColumn(Modifier.fillMaxSize()) {
        item { TitleWithHelp("وقت القلق", toolHelp["worry"] ?: "") }
        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                LabeledField("موعدك اليومي المخصص للقلق", timeInput, { timeInput = it }, "مثال: 6:00 مساءً", singleLine = true)
                SecondaryButton("حفظ الموعد", { vm.saveWorryTime(timeInput.trim()) }, Modifier.fillMaxWidth())
                Spacer(Modifier.height(10.dp))
                MutedText(if (!savedTime?.value.isNullOrBlank()) "موعدك المحدد: ${savedTime?.value} — أجّل قلقك اليومي إله." else "ما حددت موعد بعد.")
            }
        }
        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                LabeledField("سجّل قلقًا الآن بدل الانشغال فيه", worryText, { worryText = it }, "شنو القلق اللي خطر ببالك؟")
                PrimaryButton("أضف للسجل", onClick = {
                    if (worryText.isNotBlank()) { vm.addWorry(worryText.trim()); worryText = "" }
                })
            }
        }
        item {
            SectionCard {
                SectionTitle("سجل القلق")
                if (worries.isEmpty()) EmptyState("لسع ماكو قلق مسجّل — زين!")
                worries.forEach { w ->
                    EntryRow(
                        meta = "${formatDate(w.date)}${if (w.resolved) " — ✅ عولجت بوقت القلق" else ""}",
                        body = w.text,
                        extra = {
                            if (!w.resolved) {
                                Spacer(Modifier.height(6.dp))
                                SecondaryButton("تمت المعالجة", { vm.resolveWorry(w) })
                            }
                        },
                        onDelete = { vm.deleteWorry(w.id) }
                    )
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

/* ---------------- Relapse Prevention ---------------- */
@Composable
private fun RelapseSection(vm: AppViewModel) {
    val plan by vm.relapsePlan.collectAsState()
    var warning by remember { mutableStateOf("") }
    var triggers by remember { mutableStateOf("") }
    var strategies by remember { mutableStateOf("") }
    var support by remember { mutableStateOf("") }
    var firstStep by remember { mutableStateOf("") }

    LaunchedEffect(plan) {
        plan?.let {
            warning = it.warning; triggers = it.triggers; strategies = it.strategies
            support = it.support; firstStep = it.firstStep
        }
    }

    LazyColumn(Modifier.fillMaxSize()) {
        item { TitleWithHelp("خطة الوقاية", toolHelp["relapse"] ?: "") }
        item {
            SectionCard {
                LabeledField("علامات الإنذار المبكر — شلون تعرف إن حالتك بديت تسوء؟", warning, { warning = it })
                LabeledField("المواقف أو الأفكار المحفزة", triggers, { triggers = it })
                LabeledField("استراتيجيات نفعت وياك بالماضي", strategies, { strategies = it })
                LabeledField("أشخاص أو جهات دعم تقدر تتواصل وياهم", support, { support = it })
                LabeledField("أول خطوة تسويها إذا حسّيت إنك بديت تنتكس", firstStep, { firstStep = it })
                PrimaryButton("حفظ الخطة", onClick = { vm.saveRelapsePlan(warning.trim(), triggers.trim(), strategies.trim(), support.trim(), firstStep.trim()) })
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}
