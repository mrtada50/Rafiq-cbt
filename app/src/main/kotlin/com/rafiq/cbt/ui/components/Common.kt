package com.rafiq.cbt.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafiq.cbt.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Modifier.clickableSimple(onClick: () -> Unit): Modifier = composed {
    this.clickable(onClick = onClick)
}

fun formatDate(millis: Long): String {
    val fmt = SimpleDateFormat("d MMM, HH:mm", Locale("ar"))
    return fmt.format(Date(millis))
}

@Composable
fun SectionTitle(text: String) {
    Text(text, color = TextColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 10.dp))
}

@Composable
fun MutedText(text: String, modifier: Modifier = Modifier) {
    Text(text, color = TextMuted, fontSize = 13.sp, lineHeight = 20.sp, modifier = modifier)
}

@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .background(SurfaceColor, RoundedCornerShape(14.dp))
            .border(1.dp, LineColor, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) { content() }
}

@Composable
fun FieldLabel(text: String) {
    Text(text, color = TextMuted, fontSize = 12.5.sp, modifier = Modifier.padding(bottom = 6.dp))
}

@Composable
fun LabeledField(label: String, value: String, onChange: (String) -> Unit, placeholder: String = "", singleLine: Boolean = false, minLines: Int = 2) {
    FieldLabel(label)
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(placeholder, color = TextMuted) },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else minLines,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = SurfaceColor2, unfocusedContainerColor = SurfaceColor2,
            focusedTextColor = TextColor, unfocusedTextColor = TextColor,
            focusedBorderColor = GreenDim, unfocusedBorderColor = LineColor,
            cursorColor = GreenColor
        ),
        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)
    )
}

@Composable
fun LabeledSlider(label: String, value: Int, range: ClosedFloatingPointRange<Float>, onChange: (Int) -> Unit, minLabel: String = "0", maxLabel: String? = null) {
    FieldLabel(label)
    Slider(
        value = value.toFloat(), onValueChange = { onChange(it.toInt()) }, valueRange = range,
        colors = SliderDefaults.colors(thumbColor = TextColor, activeTrackColor = AmberDim, inactiveTrackColor = LineColor)
    )
    Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(minLabel, color = TextMuted, fontSize = 12.sp)
        Text("$value", color = TextColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(maxLabel ?: range.endInclusive.toInt().toString(), color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = GreenDim, contentColor = Color(0xFFEFF3F0)),
        shape = RoundedCornerShape(10.dp)
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick, enabled = enabled, modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextColor),
        border = BorderStroke(1.dp, LineColor), shape = RoundedCornerShape(10.dp)
    ) { Text(text, fontSize = 12.5.sp) }
}

@Composable
fun DangerButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick, modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = RustColor),
        border = BorderStroke(1.dp, RustDim), shape = RoundedCornerShape(10.dp)
    ) { Text(text, fontSize = 12.5.sp) }
}

@Composable
fun EmptyState(text: String) {
    Box(Modifier.fillMaxWidth().padding(22.dp), contentAlignment = Alignment.Center) {
        Text(text, color = TextMuted, fontSize = 13.sp)
    }
}

@Composable
fun EntryRow(meta: String, body: String, extra: (@Composable ColumnScope.() -> Unit)? = null, onDelete: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(meta, color = TextMuted, fontSize = 11.5.sp)
            Spacer(Modifier.height(3.dp))
            Text(body, color = TextColor, fontSize = 13.5.sp, lineHeight = 19.sp)
            extra?.invoke(this)
        }
        IconButtonX(onClick = onDelete)
    }
}

@Composable
fun IconButtonX(onClick: () -> Unit) {
    TextButton(onClick = onClick) { Text("✕", color = TextMuted) }
}

@Composable
fun MultiChipRow(items: List<Pair<String, String>>, selected: Set<String>, onToggle: (String) -> Unit) {
    FlowRowSimple {
        items.forEach { (id, name) ->
            val isSel = selected.contains(id)
            Chip(name, isSel, RustDim) { onToggle(id) }
        }
    }
}

@Composable
fun SingleChipRow(items: List<String>, selected: String, onSelect: (String) -> Unit) {
    FlowRowSimple {
        items.forEach { name ->
            val isSel = selected == name
            Chip(name, isSel, GreenDim) { onSelect(name) }
        }
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, activeColor: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .padding(end = 8.dp, bottom = 8.dp)
            .background(if (selected) activeColor else Color.Transparent, RoundedCornerShape(20.dp))
            .border(1.dp, if (selected) activeColor else LineColor, RoundedCornerShape(20.dp))
            .clickableSimple(onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp)
    ) {
        Text(text, color = if (selected) Color(0xFFF4E9E6) else TextMuted, fontSize = 12.5.sp)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun FlowRowSimple(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) { content() }
}

@Composable
fun SubTabRow(items: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 14.dp)) {
        items.forEach { (id, label) ->
            val isSel = selected == id
            Box(
                Modifier
                    .padding(end = 8.dp)
                    .background(if (isSel) GreenDim else SurfaceColor, RoundedCornerShape(20.dp))
                    .border(1.dp, if (isSel) GreenDim else LineColor, RoundedCornerShape(20.dp))
                    .clickableSimple { onSelect(id) }
                    .padding(horizontal = 15.dp, vertical = 8.dp)
            ) {
                Text(label, color = if (isSel) Color(0xFFEFF3F0) else TextMuted, fontSize = 12.5.sp)
            }
        }
    }
}

@Composable
fun StatCard(number: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(SurfaceColor, RoundedCornerShape(14.dp))
            .border(1.dp, LineColor, RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(number, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Text(label, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
fun ToolLink(icon: String, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .background(SurfaceColor, RoundedCornerShape(14.dp))
            .border(1.dp, LineColor, RoundedCornerShape(14.dp))
            .clickableSimple(onClick)
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 21.sp)
        Spacer(Modifier.height(6.dp))
        Text(label, color = TextColor, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

@Composable
fun AccordionItem(title: String, expanded: Boolean, onToggle: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().border(width = 0.dp, color = Color.Transparent)) {
        Row(
            Modifier.fillMaxWidth().clickableSimple(onToggle).padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = TextColor, fontSize = 13.5.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text(if (expanded) "▴" else "▾", color = TextMuted, fontSize = 12.sp)
        }
        if (expanded) Column(Modifier.padding(bottom = 14.dp)) { content() }
        Divider(color = LineColor, thickness = 1.dp)
    }
}

@Composable
fun ExampleBox(text: String) {
    Text(
        text, color = TextColor, fontSize = 12.5.sp,
        modifier = Modifier
            .padding(top = 8.dp)
            .background(SurfaceColor2, RoundedCornerShape(8.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp)
    )
}

@Composable
fun CenteredNumberBadge(text: String) {
    Box(
        Modifier.size(44.dp).background(SurfaceColor2, CircleShape),
        contentAlignment = Alignment.Center
    ) { Text(text, color = AmberColor, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun ChainStepBox(text: String) {
    Text(
        text, color = TextColor, fontSize = 13.5.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceColor2, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    )
}

@Composable
fun ChainArrow() {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text("↓", color = TextMuted, fontSize = 14.sp, modifier = Modifier.padding(vertical = 3.dp))
    }
}

@Composable
fun ChainCoreBox(text: String) {
    Text(
        text, color = Color(0xFFF4E9E6), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .background(RustDim, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    )
}

@Composable
fun TitleWithHelp(title: String, helpText: String) {
    var show by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = TextColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Box(
            Modifier.size(22.dp).background(SurfaceColor2, CircleShape).clickableSimple { show = true },
            contentAlignment = Alignment.Center
        ) { Text("؟", color = TextMuted, fontSize = 13.sp) }
    }
    if (show) {
        AlertDialog(
            onDismissRequest = { show = false },
            containerColor = SurfaceColor,
            title = { Text(title, color = TextColor) },
            text = { Text(helpText, color = TextMuted, fontSize = 13.sp, lineHeight = 20.sp) },
            confirmButton = { TextButton(onClick = { show = false }) { Text("فهمت", color = GreenColor) } }
        )
    }
}

@Composable
fun SwitchRow(label: String, sub: String? = null, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f).padding(end = 10.dp)) {
            Text(label, color = TextColor, fontSize = 14.sp)
            if (sub != null) Text(sub, color = TextMuted, fontSize = 11.5.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Switch(
            checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = GreenDim, checkedThumbColor = GreenColor, uncheckedTrackColor = SurfaceColor2)
        )
    }
}

@Composable
fun DotsIndicator(total: Int, current: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        repeat(total) { i ->
            Box(
                Modifier
                    .padding(horizontal = 3.dp)
                    .size(6.dp)
                    .background(if (i == current) AmberColor else LineColor, CircleShape)
            )
        }
    }
}
