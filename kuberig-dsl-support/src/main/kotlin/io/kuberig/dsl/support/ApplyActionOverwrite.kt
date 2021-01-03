package io.kuberig.dsl.support

import io.kuberig.annotations.ApplyAction

sealed class ApplyActionOverwrite
object UseDefault : ApplyActionOverwrite()
data class UseOverwrite(val action: ApplyAction) : ApplyActionOverwrite()
