package com.bintianqi.hookdpm.hook

import android.util.Log
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit

@InjectYukiHookWithXposed
object HookEntry : IYukiHookXposedInit {
    override fun onHook() = encase {
        loadSystem{
            val dpms = "com.android.server.devicepolicy.DevicePolicyManagerService".toClass()
            dpms.method {
                name = "hasIncompatibleAccountsOnAnyUser"
                emptyParam()
                returnType = BooleanType
            }.hook {
                after {
                    if(prefs.getBoolean("hook_hiaoau", false)) {
                        result = false
                    }
                }
            }
            dpms.method {
                name = "enforceCanSetProfileOwnerLocked"
                paramCount = 4
                returnType = UnitType
            }.hook {
                if(prefs.getBoolean("hook_ecspol", false)) {
                    Log.d("HookDPM", "ECSPOL hooked")
                    replaceUnit { }
                }
            }
            dpms.method {
                name = "nonTestNonPrecreatedUsersExist"
                emptyParam()
                returnType = BooleanType
            }.hook {
                after {
                    if(prefs.getBoolean("hook_ntnpue", false)) {
                        result = false
                    }
                }
            }
            dpms.method {
                name = "isProvisioningAllowed"
                param(StringClass, StringClass)
                returnType = BooleanType
            }.hook {
                after {
                    if(prefs.getBoolean("hook_ipa", false)) {
                        result = true
                    }
                }
            }
            dpms.method {
                name = "checkProvisioningPrecondition"
                param(StringClass, StringClass)
                returnType = IntType
            }.hook {
                after {
                    if(prefs.getBoolean("hook_cpp", false)) {
                        result = 0
                    }
                }
            }
        }
    }
}
