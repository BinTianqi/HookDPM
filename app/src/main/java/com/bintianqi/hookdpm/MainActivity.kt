package com.bintianqi.hookdpm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.core.view.isVisible
import com.bintianqi.hookdpm.R
import com.highcapable.yukihookapi.YukiHookAPI

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
	setContent {
            Text(text = "HookDPM")
	}
    }

    /*private fun hideOrShowLauncherIcon(isShow: Boolean) {
        packageManager?.setComponentEnabledSetting(
            ComponentName(packageName, "${packageName}.Home"),
            if (isShow) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private val isLauncherIconShowing
        get() = packageManager?.getComponentEnabledSetting(
            ComponentName(packageName, "${packageName}.Home")
        ) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED

    private fun refreshModuleStatus() {
        if (YukiHookAPI.Status.isXposedModuleActive) 
    }*/
}
