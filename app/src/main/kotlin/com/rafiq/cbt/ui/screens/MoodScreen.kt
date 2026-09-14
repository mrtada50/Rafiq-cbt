package com.rafiq.cbt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.rafiq.cbt.AppViewModel
import com.rafiq.cbt.data.toolHelp
import com.rafiq.cbt.ui.components.*
import com.rafiq.cbt.ui.theme.AmberColor
import com.rafiq.cbt.ui.theme.AmberDim
import com.rafiq.cbt.ui.theme.TextMuted

private val moodFaces = listOf("😞","😞","😕","😕","😐","😐","🙂","🙂","😊","😄","😄")

@Composable
fun MoodScreen(vm: AppViewModel) {
    val moods by vm.moods.collectAsState()
    var value by remember { mutableStateOf(5) }
    var note by remember { mutableStateOf("") }

    LazyColumn(Modifier.fillMaxSize().padding(18.dp, 16.dp)) {
        item { TitleWithHelp("كيف تشعر الآن؟", toolHelp["mood"] ?: "") }
        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(moodFaces[value], fontSize = 40.sp)
                }
                Spacer(Modifier.height(6.dp))
                LabeledSlider("", value, 0f..10f, { value = it }, "سيئ جدًا", "ممتاز")
                LabeledField("ملاحظة سريعة (اختياري)", note, { note = it }, "شنو صاير بيومك اليوم؟")
                PrimaryButton("حفظ الحالة المزاجية", onClick = {
                    vm.addMood(value, note.trim())
                    note = ""
                })
            }
        }
        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                SectionTitle("آخر 14 يوم")
                val last14 = moods.takeLast(14)
                if (last14.isEmpty()) {
                    EmptyState("ما عندك تسجيلات بعد")
                } else {
                    Row(Modifier.fillMaxWidth().height(70.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        last14.forEachIndexed { i, m ->
                            val h = (m.value / 10f * 70).dp.coerceAtLeast(4.dp)
                            Box(
                                Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    Modifier.fillMaxWidth().height(h)
                                        .background(if (i == last14.lastIndex) AmberColor else AmberDim, RoundedCornerShape(3.dp, 3.dp, 0.dp, 0.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            SectionCard {
                SectionTitle("السجل")
                val recent = moods.reversed().take(15)
                if (recent.isEmpty()) {
                    EmptyState("لسع ماكو تسجيلات — سجّل أول حالة مزاجية فوق.")
                } else {
                    recent.forEach { m ->
                        EntryRow(
                            meta = "${formatDate(m.date)} — ${m.value}/10 ${moodFaces[m.value]}",
                            body = m.note,
                            onDelete = { vm.deleteMood(m.id) }
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(20.dp)) }
    }
}
