package com.bintianqi.hookdpm

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.factory.prefs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val darkTheme = isSystemInDarkTheme()
            val view = LocalView.current
            SideEffect {
                window.statusBarColor = Color.Transparent.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
            if(VERSION.SDK_INT >= 31) {
                val colorScheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                MaterialTheme(
                    colorScheme = colorScheme
                ) {
                    Home()
                }
            } else {
                Home()
            }
	    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Home() {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding(), start = 10.dp, end = 10.dp),
        ){
            val active = YukiHookAPI.Status.isModuleActive
            // HIAOAU: hasIncompatibleAccountsOnAnyUser
            // IPA: isProvisioningAllowed
            // CPP: checkProvisioningPreCondition
            var hookHIAOAU by remember { mutableStateOf(false) }
            var hookIPA by remember { mutableStateOf(false) }
            var hookCPP by remember { mutableStateOf(false) }
            var hideIcon by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                hookHIAOAU = context.prefs().getBoolean("hook_hiaoau", false)
                hookIPA = context.prefs().getBoolean("hook_ipa", false)
                hookCPP = context.prefs().getBoolean("hook_cpp", false)
                hideIcon = isLauncherIconHiding(context)
            }
            Text(text = "Module active: $active")
            SwitchItem(
                text = "Hide Launcher icon",
                checked = hideIcon,
                onCheckedChange = {
                    context.packageManager?.setComponentEnabledSetting(
                        ComponentName(context.packageName, "${context.packageName}.Home"),
                        if (it) PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                    )
                    hideIcon = isLauncherIconHiding(context)
                }
            )
            SwitchItem(
                text = "Bypass accounts limit",
                checked = hookHIAOAU,
                onCheckedChange = {
                    context.prefs().edit{ putBoolean("hook_hiaoau", it) }
                    hookHIAOAU = context.prefs().getBoolean("hook_hiaoau", false)
                }
            )
            SwitchItem(
                text = "Always allow creating work profile",
                checked = hookIPA,
                onCheckedChange = {
                    context.prefs().edit{ putBoolean("hook_ipa", it) }
                    hookIPA = context.prefs().getBoolean("hook_ipa", false)
                }
            )
            SwitchItem(
                text = "Skip provisioning check",
                checked = hookCPP,
                onCheckedChange = {
                    context.prefs().edit{ putBoolean("hook_cpp", it) }
                    hookCPP = context.prefs().getBoolean("hook_cpp", false)
                }
            )
        }
    }
}

@Composable
private fun SwitchItem(
    text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

private fun isLauncherIconHiding(context: Context):Boolean {
    return context.packageManager?.getComponentEnabledSetting(
        ComponentName(context.packageName, "${context.packageName}.Home")
    ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
}

