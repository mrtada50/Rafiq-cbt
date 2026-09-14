package com.rafiq.cbt

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.rafiq.cbt.ui.screens.*
import com.rafiq.cbt.ui.theme.*

class MainActivity : FragmentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RafiqTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppRoot(vm, onAuthenticate = { onResult -> showBiometricPrompt(onResult) })
                }
            }
        }
    }

    private fun showBiometricPrompt(onResult: (Boolean) -> Unit) {
        val manager = BiometricManager.from(this)
        val canAuth = manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        if (canAuth != BiometricManager.BIOMETRIC_SUCCESS) {
            onResult(true)
            return
        }
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onResult(true)
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onResult(false)
            }
        })
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("فتح رفيق")
            .setSubtitle("استخدم بصمتك لفتح التطبيق")
            .setNegativeButtonText("إلغاء")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        prompt.authenticate(promptInfo)
    }
}

private data class NavItem(val id: String, val icon: String, val label: String)

private val navItems = listOf(
    NavItem("home", "🏠", "الرئيسية"),
    NavItem("mood", "🌙", "المزاج"),
    NavItem("thoughts", "💭", "الأفكار"),
    NavItem("tools", "🧰", "الأدوات"),
    NavItem("relax", "🌬️", "الاسترخاء")
)

@Composable
fun AppRoot(vm: AppViewModel, onAuthenticate: ((Boolean) -> Unit) -> Unit) {
    val lockRequired by vm.biometricLockEnabled.collectAsState()
    var unlocked by remember { mutableStateOf(false) }

    LaunchedEffect(lockRequired) {
        if (lockRequired && !unlocked) onAuthenticate { success -> unlocked = success }
    }

    if (lockRequired && !unlocked) {
        LockScreen { onAuthenticate { success -> unlocked = success } }
        return
    }

    var mainTab by remember { mutableStateOf("home") }
    var thoughtsSub by remember { mutableStateOf("record") }
    var toolsSub by remember { mutableStateOf("activation") }
    var relaxSub by remember { mutableStateOf("breath") }

    fun navigate(tab: String, sub: String? = null) {
        mainTab = tab
        when (tab) {
            "thoughts" -> sub?.let { thoughtsSub = it }
            "tools" -> sub?.let { toolsSub = it }
            "relax" -> sub?.let { relaxSub = it }
        }
    }

    Scaffold(
        containerColor = BgColor,
        topBar = {
            Row(
                Modifier.fillMaxWidth().background(BgColor).padding(20.dp, 18.dp, 20.dp, 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("رفيق", color = TextColor, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                    Text("أدوات العلاج المعرفي السلوكي بين يديك", color = TextMuted, fontSize = 12.5.sp)
                }
                IconButton(onClick = { navigate("settings") }) {
                    Text("⚙", color = TextMuted, fontSize = 16.sp)
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = SurfaceColor, contentColor = TextColor) {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = mainTab == item.id,
                        onClick = { mainTab = item.id },
                        icon = { Text(item.icon, fontSize = 18.sp) },
                        label = { Text(item.label, fontSize = 10.5.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GreenColor, selectedTextColor = GreenColor,
                            unselectedIconColor = TextMuted, unselectedTextColor = TextMuted,
                            indicatorColor = SurfaceColor2
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize().background(BgColor)) {
            androidx.compose.animation.Crossfade(targetState = mainTab, label = "tabSwitch") { tab ->
                when (tab) {
                    "home" -> HomeScreen(vm, onNavigate = { t, s -> navigate(t, s) })
                    "mood" -> MoodScreen(vm)
                    "thoughts" -> ThoughtsScreen(vm, thoughtsSub) { thoughtsSub = it }
                    "tools" -> ToolsScreen(vm, toolsSub) { toolsSub = it }
                    "relax" -> RelaxScreen(vm, relaxSub) { relaxSub = it }
                    "review" -> ReviewScreen(vm)
                    "search" -> SearchScreen(vm)
                    "settings" -> SettingsScreen(vm)
                }
            }
        }
    }
}

@Composable
private fun LockScreen(onUnlockRequest: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(BgColor).padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🔒", fontSize = 46.sp)
            Spacer(Modifier.height(16.dp))
            Text("رفيق مقفول", color = TextColor, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text("افتحه ببصمتك للمتابعة", color = TextMuted, fontSize = 13.sp)
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onUnlockRequest,
                colors = ButtonDefaults.buttonColors(containerColor = GreenDim, contentColor = androidx.compose.ui.graphics.Color(0xFFEFF3F0))
            ) { Text("فتح ببصمتك") }
        }
    }
}
