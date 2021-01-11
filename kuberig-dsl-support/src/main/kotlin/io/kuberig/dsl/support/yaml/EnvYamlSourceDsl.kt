package io.kuberig.dsl.support.yaml

import io.kuberig.dsl.KubeRigDslMarker

@KubeRigDslMarker
class EnvYamlSourceDsl(val location: String) {

    private var targetNamespace : EnvYamlSourceTargetNamespace = UseEnvironmentDefault

    fun useEnvironmentDefaultNamespace() {
        this.targetNamespace = UseEnvironmentDefault
    }

    fun useNamespace(namespace: String) {
        this.targetNamespace = UseSpecificNamespace(namespace)
    }

    fun toSource(): EnvYamlSource {
        return EnvYamlSource(location, targetNamespace)
    }
}

fun envYamlSource(location: String, init: EnvYamlSourceDsl.() -> Unit): EnvYamlSourceDsl {
    val dsl = EnvYamlSourceDsl(location)
    dsl.init()
    return dsl
}