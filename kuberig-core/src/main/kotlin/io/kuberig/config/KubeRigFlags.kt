package io.kuberig.config

open class KubeRigFlags(
    val trustSelfSignedSSL: Boolean = false,
    val trustAllSSL: Boolean = false,
    var applyStrategyFlags: ApplyStrategyFlags = ServerSideApplyFlags(force=false)
) {
    fun serverSideApply(init: ServerSideApplyFlags.() -> Unit) {
        val newFlags = ServerSideApplyFlags()
        newFlags.init()
        this.applyStrategyFlags = newFlags
    }

    fun clientSideApply(init: ClientSideApplyFlags.() -> Unit) {
        val newFlags = ClientSideApplyFlags()
        newFlags.init()
        this.applyStrategyFlags = newFlags
    }
}

sealed class ApplyStrategyFlags
open class ServerSideApplyFlags(var force: Boolean = false) : ApplyStrategyFlags()
open class ClientSideApplyFlags(var recreateAllowed: Boolean = true): ApplyStrategyFlags()