package com.rafiq.cbt.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.cbt.AppViewModel
import com.rafiq.cbt.data.groundSteps
import com.rafiq.cbt.data.pmrSteps
import com.rafiq.cbt.data.toolHelp
import com.rafiq.cbt.ui.components.*
import com.rafiq.cbt.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RelaxScreen(vm: AppViewModel, sub: String, onSubChange: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp, 16.dp)) {
        SubTabRow(
            items = listOf("breath" to "التنفس", "ground" to "التأريض", "pmr" to "استرخاء عضلي"),
            selected = sub, onSelect = onSubChange
        )
        when (sub) {
            "breath" -> BreathingSection()
            "ground" -> GroundingSection()
            "pmr" -> PmrSection()
        }
    }
}

/* ---------------- Breathing ---------------- */
private data class BreathPhase(val label: String, val sub: String, val expand: Boolean)
private val breathPhases = listOf(
    BreathPhase("شهيق", "خذ نفس ببطء من أنفك", true),
    BreathPhase("احتفاظ", "خلّي هوائك بصدرك", true),
    BreathPhase("زفير", "أخرج الهوا ببطء من فمك", false),
    BreathPhase("احتفاظ", "قبل ما تبدأ من جديد", false)
)

@Composable
private fun BreathingSection() {
    var breathing by remember { mutableStateOf(false) }
    var phaseIdx by remember { mutableStateOf(0) }
    var cycles by remember { mutableStateOf(0) }
    val scale = remember { Animatable(0.7f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(breathing) {
        if (breathing) {
            phaseIdx = 0; cycles = 0
            while (breathing) {
                val phase = breathPhases[phaseIdx]
                scope.launch { scale.animateTo(if (phase.expand) 1f else 0.7f, tween(3800)) }
                delay(4000)
                phaseIdx = (phaseIdx + 1) % breathPhases.size
                if (phaseIdx == 0) cycles++
            }
        } else {
            scale.animateTo(0.7f, tween(300))
        }
    }

    val currentPhase = breathPhases[phaseIdx]

    LazyColumn(Modifier.fillMaxSize()) {
        item { TitleWithHelp("تمرين التنفس", toolHelp["breath"] ?: "") }
        item {
            SectionCard {
                Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(150.dp).scale(scale.value)
                            .background(
                                androidx.compose.ui.graphics.Brush.radialGradient(listOf(AmberDim, GreenDim)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (breathing) currentPhase.label else "ابدأ", color = Color(0xFFF3EFE7), fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text(if (breathing) currentPhase.sub else "اضغط للبدء", color = Color(0xFFE9DFCF), fontSize = 12.sp)
                        }
                    }
                    if (breathing) {
                        Spacer(Modifier.height(16.dp))
                        Text("الدورات المكتملة: $cycles", color = TextMuted, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton(if (breathing) "إيقاف التمرين" else "ابدأ التمرين", onClick = { breathing = !breathing })
                    Spacer(Modifier.height(10.dp))
                    MutedText("شهيق 4 ثواني ← حبس 4 ثواني ← زفير 4 ثواني ← حبس 4 ثواني، وتتكرر الدورة.")
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

/* ---------------- Grounding ---------------- */
@Composable
private fun GroundingSection() {
    var index by remember { mutableStateOf(0) }
    var note by remember { mutableStateOf("") }
    val s = groundSteps[index]

    LazyColumn(Modifier.fillMaxSize()) {
        item { TitleWithHelp("التأريض 5-4-3-2-1", toolHelp["ground"] ?: "") }
        item {
            SectionCard {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    CenteredNumberBadge(s.icon)
                    Spacer(Modifier.height(12.dp))
                    Text("سمِّ ${s.n} ${s.sense}", color = TextColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    MutedText(s.hint)
                    Spacer(Modifier.height(10.dp))
                    LabeledField("", note, { note = it }, "اكتب وياها إذا حبيت (اختياري)")
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryButton("السابق", { if (index > 0) { index--; note = "" } }, enabled = index > 0)
                        PrimaryButton(if (index == groundSteps.lastIndex) "إنهاء التمرين" else "التالي", {
                            if (index == groundSteps.lastIndex) index = 0 else index++
                            note = ""
                        }, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))
                    DotsIndicator(groundSteps.size, index)
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

/* ---------------- PMR ---------------- */
@Composable
private fun PmrSection() {
    var index by remember { mutableStateOf(0) }
    var running by remember { mutableStateOf(false) }
    var phaseLabel by remember { mutableStateOf("") }
    var remaining by remember { mutableStateOf(0) }
    val s = pmrSteps[index]

    LazyColumn(Modifier.fillMaxSize()) {
        item { TitleWithHelp("استرخاء عضلي تدريجي", toolHelp["pmr"] ?: "") }
        item {
            SectionCard {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    CenteredNumberBadge("${index + 1}")
                    Spacer(Modifier.height(12.dp))
                    Text(s.group, color = TextColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    MutedText(s.instr)
                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                        if (remaining > 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("$remaining", color = AmberColor, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                                Text(phaseLabel, color = TextMuted, fontSize = 12.5.sp)
                            }
                        } else if (phaseLabel.isNotEmpty()) {
                            Text(phaseLabel, color = TextMuted, fontSize = 12.5.sp)
                        }
                    }
                    SecondaryButton("ابدأ الشد والإرخاء", {
                        if (!running) runPmrCountdown(
                            onRunning = { running = it },
                            onTick = { r, l -> remaining = r; phaseLabel = l },
                            onDone = { phaseLabel = "تم ✓ — انتقل للمجموعة التالية"; remaining = 0 }
                        )
                    }, Modifier.fillMaxWidth(), enabled = !running)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryButton("السابق", { if (index > 0) { index--; phaseLabel = ""; remaining = 0 } }, enabled = index > 0)
                        PrimaryButton(if (index == pmrSteps.lastIndex) "إنهاء" else "التالي", {
                            if (index == pmrSteps.lastIndex) index = 0 else index++
                            phaseLabel = ""; remaining = 0
                        }, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(16.dp))
                    DotsIndicator(pmrSteps.size, index)
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

private var pmrJob: kotlinx.coroutines.Job? = null

private fun runPmrCountdown(onRunning: (Boolean) -> Unit, onTick: (Int, String) -> Unit, onDone: () -> Unit) {
    pmrJob?.cancel()
    pmrJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
        onRunning(true)
        for (t in 5 downTo 1) { onTick(t, "شد العضلات"); delay(1000) }
        for (t in 10 downTo 1) { onTick(t, "إرخاء وتنفّس"); delay(1000) }
        onDone()
        onRunning(false)
    }
}
