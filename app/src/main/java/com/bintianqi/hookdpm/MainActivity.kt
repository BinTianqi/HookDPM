package com.bintianqi.hookdpm

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.bintianqi.hookdpm.ui.icons.filled.Error
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
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                    !darkTheme
            }
            if (VERSION.SDK_INT >= 31) {
                val colorScheme =
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(
                        context
                    )
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
                .padding(paddingValues),
        ) {
            val active = YukiHookAPI.Status.isXposedModuleActive
            // IPA: isProvisioningAllowed
            // CPP: checkProvisioningPreCondition
            var forceDO by remember { mutableStateOf(false) }
            var forcePO by remember { mutableStateOf(false) }
            var hookIPA by remember { mutableStateOf(false) }
            var hookCPP by remember { mutableStateOf(false) }
            var hideIcon by remember { mutableStateOf(false) }
            var bypassAccountCheck by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                if (active) {
                    forceDO = context.prefs().getBoolean("force_do", false)
                    forcePO = context.prefs().getBoolean("force_po", false)
                    hookIPA = context.prefs().getBoolean("hook_ipa", false)
                    hookCPP = context.prefs().getBoolean("hook_cpp", false)
                    bypassAccountCheck = context.prefs().getBoolean("bypass_account_check", false)
                }
                hideIcon = isLauncherIconHiding(context)
            }

            Spacer(Modifier.padding(vertical = 4.dp))
            StatusCard(active)
            Spacer(Modifier.padding(vertical = 6.dp))

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

            if (active) {
                Text(
                    modifier = Modifier.padding(horizontal = 15.dp),
                    text = "Hook",
                    style = MaterialTheme.typography.titleLarge
                )
                SwitchItem(
                    text = stringResource(R.string.bypass_account_check),
                    checked = bypassAccountCheck,
                    onCheckedChange = {
                        context.prefs().edit { putBoolean("bypass_account_check", it) }
                        bypassAccountCheck = it
                    }
                )
                Spacer(Modifier.padding(vertical = 10.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 15.dp),
                    text = stringResource(R.string.danger_zone),
                    style = MaterialTheme.typography.titleLarge
                )
                SwitchItem(
                    text = stringResource(R.string.force_set_device_owner),
                    checked = forceDO,
                    onCheckedChange = {
                        context.prefs().edit { putBoolean("force_do", it) }
                        forceDO = it
                    }
                )
                SwitchItem(
                    text = stringResource(R.string.force_set_profile_owner),
                    checked = forcePO,
                    onCheckedChange = {
                        context.prefs().edit { putBoolean("force_po", it) }
                        forcePO = it
                    }
                )
                SwitchItem(
                    text = stringResource(R.string.always_allow_provisioning),
                    checked = hookIPA,
                    onCheckedChange = {
                        context.prefs().edit { putBoolean("hook_ipa", it) }
                        hookIPA = it
                    }
                )
                SwitchItem(
                    text = stringResource(R.string.skip_provisioning_check),
                    checked = hookCPP,
                    onCheckedChange = {
                        context.prefs().edit { putBoolean("hook_cpp", it) }
                        hookCPP = it
                    }
                )
            }
        }
    }
}

@Composable
fun StatusCard(active: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (active) Icons.Rounded.CheckCircle else Icons.Default.Error,
                contentDescription = if (active) stringResource(R.string.module_activated) else stringResource(R.string.module_not_activated),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(24.dp))

            Column {
                Text(
                    text = stringResource(if (active) R.string.module_activated else R.string.module_not_activated),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Text(
                    text = stringResource(R.string.module_version_is) + BuildConfig.VERSION_NAME,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun SwitchItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheckedChange(!checked)
            }
            .padding(horizontal = 12.dp, vertical = 6.dp),
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

private fun isLauncherIconHiding(context: Context): Boolean {
    return context.packageManager?.getComponentEnabledSetting(
        ComponentName(context.packageName, "${context.packageName}.Home")
    ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
}

