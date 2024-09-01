package com.bintianqi.hookdpm

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                .verticalScroll(rememberScrollState())
                .padding(top = paddingValues.calculateTopPadding(), start = 10.dp, end = 10.dp),
        ){
            val active = YukiHookAPI.Status.isXposedModuleActive
            // HIAOAU: hasIncompatibleAccountsOnAnyUser
            // NTNPUE: nonTestNonPrecreatedUsersExist
            // ECSPOL: enforceCanSetProfileOwnerLocked
            // IPA: isProvisioningAllowed
            // CPP: checkProvisioningPreCondition
            var forceDO by remember { mutableStateOf(false) }
            var forcePO by remember { mutableStateOf(false) }
            var hookIPA by remember { mutableStateOf(false) }
            var hookCPP by remember { mutableStateOf(false) }
            var enhancedMode by remember { mutableStateOf(false) }
            var hideIcon by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                if(active) {
                    forceDO = context.prefs().getBoolean("force_do", false)
                    forcePO = context.prefs().getBoolean("force_po", false)
                    enhancedMode = context.prefs().getBoolean("enhanced_mode", false)
                    hookIPA = context.prefs().getBoolean("hook_ipa", false)
                    hookCPP = context.prefs().getBoolean("hook_cpp", false)
                }
                hideIcon = isLauncherIconHiding(context)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(15))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(10.dp)
            ) {
                Text(
                    text = stringResource(if(active) R.string.module_activated else R.string.module_not_activated),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = stringResource(R.string.module_version_is) + BuildConfig.VERSION_NAME,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            SwitchItem(
                text = stringResource(R.string.hide_launcher_icon),
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
            Spacer(Modifier.padding(vertical = 10.dp))
            if(active) {
                Text(
                    text = "Hook",
                    style = MaterialTheme.typography.titleLarge
                )
                SwitchItem(
                    text = stringResource(R.string.force_set_device_owner),
                    checked = forceDO,
                    onCheckedChange = {
                        context.prefs().edit{ putBoolean("force_do", it) }
                        forceDO = context.prefs().getBoolean("force_do", false)
                    }
                )
                SwitchItem(
                    text = stringResource(R.string.force_set_profile_owner),
                    checked = forcePO,
                    onCheckedChange = {
                        context.prefs().edit{ putBoolean("force_po", it) }
                        forcePO = context.prefs().getBoolean("force_po", false)
                    }
                )
                SwitchItem(
                    text = stringResource(R.string.enhanced_mode),
                    checked = enhancedMode,
                    onCheckedChange = {
                        context.prefs().edit{ putBoolean("enhanced_mode", it) }
                        enhancedMode = context.prefs().getBoolean("enhanced_mode", false)
                    }
                )
                Spacer(Modifier.padding(vertical = 10.dp))
                Text(
                    text = stringResource(R.string.danger_zone),
                    style = MaterialTheme.typography.titleLarge
                )
                SwitchItem(
                    text = stringResource(R.string.always_allow_provisioning),
                    checked = hookIPA,
                    onCheckedChange = {
                        context.prefs().edit{ putBoolean("hook_ipa", it) }
                        hookIPA = context.prefs().getBoolean("hook_ipa", false)
                    }
                )
                SwitchItem(
                    text = stringResource(R.string.skip_provisioning_check),
                    checked = hookCPP,
                    onCheckedChange = {
                        context.prefs().edit{ putBoolean("hook_cpp", it) }
                        hookCPP = context.prefs().getBoolean("hook_cpp", false)
                    }
                )
            }
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

