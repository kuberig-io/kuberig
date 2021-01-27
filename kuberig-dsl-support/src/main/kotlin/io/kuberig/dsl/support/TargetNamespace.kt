package io.kuberig.dsl.support

sealed class TargetNamespace
object UseSourceOrEnvironmentDefault : TargetNamespace()
object UseSource : TargetNamespace()
object UseEnvironmentDefault : TargetNamespace()
data class UseSpecific(val namespace: String) : TargetNamespace()
