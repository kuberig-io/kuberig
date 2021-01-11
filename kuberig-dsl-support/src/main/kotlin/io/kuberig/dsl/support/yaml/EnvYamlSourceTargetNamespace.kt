package io.kuberig.dsl.support.yaml

sealed class EnvYamlSourceTargetNamespace
object UseEnvironmentDefault : EnvYamlSourceTargetNamespace()
data class UseSpecificNamespace(val namespace: String) : EnvYamlSourceTargetNamespace()
