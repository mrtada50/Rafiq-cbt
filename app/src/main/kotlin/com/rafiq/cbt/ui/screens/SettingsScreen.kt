package com.rafiq.cbt.ui.screens

import android.app.TimePickerDialog
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafiq.cbt.AppViewModel
import com.rafiq.cbt.ReminderScheduler
import com.rafiq.cbt.data.exportBundleToJson
import com.rafiq.cbt.data.parseImportJson
import com.rafiq.cbt.ui.components.*
import com.rafiq.cbt.ui.theme.GreenColor
import com.rafiq.cbt.ui.theme.RustColor

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val context = LocalContext.current
    val biometricEnabled by vm.biometricLockEnabled.collectAsState()

    var reminderEnabled by remember { mutableStateOf(ReminderScheduler.isEnabled(context)) }
    var reminderHour by remember { mutableStateOf(ReminderScheduler.getHour(context)) }
    var reminderMinute by remember { mutableStateOf(ReminderScheduler.getMinute(context)) }

    var showResetConfirm by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<Uri?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun openTimePicker() {
        TimePickerDialog(context, { _, h, m ->
            reminderHour = h; reminderMinute = m; reminderEnabled = true
            ReminderScheduler.schedule(context, h, m)
        }, reminderHour, reminderMinute, true).show()
    }

    val notifPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) openTimePicker()
        else statusMessage = "لازم صلاحية الإشعارات حتى يشتغل التذكير"
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            try {
                val json = exportBundleToJson(vm.exportData())
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                statusMessage = "تم تصدير نسخة احتياطية بنجاح"
            } catch (e: Exception) {
                statusMessage = "صار خطأ أثناء التصدير"
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) pendingImportUri = uri
    }

    LazyColumn(Modifier.fillMaxSize().padding(18.dp, 16.dp)) {
        item { SectionTitle("الإعدادات") }

        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                SwitchRow(
                    "قفل بالبصمة", "يطلب بصمتك كل مرة تفتح فيها التطبيق",
                    biometricEnabled
                ) { vm.setBiometricLock(it) }
            }
        }

        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                SwitchRow(
                    "تذكير يومي لتسجيل المزاج",
                    if (reminderEnabled) "الساعة %02d:%02d".format(reminderHour, reminderMinute) else "متوقف حاليًا",
                    reminderEnabled
                ) { checked ->
                    if (checked) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notifPermLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            openTimePicker()
                        }
                    } else {
                        reminderEnabled = false
                        ReminderScheduler.cancel(context)
                    }
                }
                if (reminderEnabled) {
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton("تغيير الوقت", { openTimePicker() }, Modifier.fillMaxWidth())
                }
            }
        }

        item {
            SectionCard(Modifier.padding(bottom = 14.dp)) {
                SectionTitle("النسخ الاحتياطي")
                MutedText("صدّر بياناتك لملف تقدر تحفظه أو تنقله لهاتف جديد.", Modifier.padding(bottom = 10.dp))
                PrimaryButton("تصدير نسخة احتياطية", onClick = {
                    exportLauncher.launch("rafiq-backup-${System.currentTimeMillis()}.json")
                })
                Spacer(Modifier.height(10.dp))
                SecondaryButton("استيراد نسخة احتياطية", {
                    importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                }, Modifier.fillMaxWidth())
            }
        }

        item {
            SectionCard {
                SectionTitle("منطقة الخطر")
                DangerButton("حذف كل البيانات", { showResetConfirm = true }, Modifier.fillMaxWidth())
            }
        }

        statusMessage?.let { msg ->
            item { MutedText(msg, Modifier.padding(top = 14.dp)) }
        }

        item { Spacer(Modifier.height(20.dp)) }
    }

    if (showResetConfirm) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = com.rafiq.cbt.ui.theme.SurfaceColor,
            title = { androidx.compose.material3.Text("حذف كل البيانات؟", color = com.rafiq.cbt.ui.theme.TextColor) },
            text = { androidx.compose.material3.Text("هذا الإجراء لا يمكن التراجع عنه.", color = com.rafiq.cbt.ui.theme.TextMuted) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { vm.resetAll(); showResetConfirm = false }) {
                    androidx.compose.material3.Text("حذف", color = RustColor)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showResetConfirm = false }) {
                    androidx.compose.material3.Text("إلغاء", color = com.rafiq.cbt.ui.theme.TextColor)
                }
            }
        )
    }

    pendingImportUri?.let { uri ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { pendingImportUri = null },
            containerColor = com.rafiq.cbt.ui.theme.SurfaceColor,
            title = { androidx.compose.material3.Text("استيراد نسخة احتياطية؟", color = com.rafiq.cbt.ui.theme.TextColor) },
            text = { androidx.compose.material3.Text("هذا راح يستبدل كل بياناتك الحالية بمحتوى الملف. متأكد؟", color = com.rafiq.cbt.ui.theme.TextMuted) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    try {
                        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        if (text != null) {
                            val bundle = parseImportJson(text)
                            vm.importData(bundle)
                            statusMessage = "تم استيراد البيانات بنجاح"
                        }
                    } catch (e: Exception) {
                        statusMessage = "الملف غير صالح أو صار خطأ بالاستيراد"
                    }
                    pendingImportUri = null
                }) { androidx.compose.material3.Text("استيراد", color = GreenColor) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { pendingImportUri = null }) {
                    androidx.compose.material3.Text("إلغاء", color = com.rafiq.cbt.ui.theme.TextColor)
                }
            }
        )
    }
}
