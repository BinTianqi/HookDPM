package com.bintianqi.hookdpm.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
object HookEntry : IYukiHookXposedInit {
    override fun onHook() = encase {
        loadSystem{
            "com.android.server.devicepolicy.DevicePolicyManagerService".toClass().method {
                name = "hasIncompatibleAccountsOnAnyUser"
                emptyParam()
                returnType = BooleanType
            }.hook {
                after {
                    if(prefs.getBoolean("hook", false)) {
                        result = false
                    }
                }
            }
        }
    }
}
