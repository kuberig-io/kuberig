package io.kuberig.dsl.support

import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.BasicResource
import io.kuberig.dsl.support.yaml.EnvYamlSource

interface DslResourceReceiver {

    fun getName() : String

    fun <T : BasicResource> receive(dslType: KubernetesResourceDslType<T>, applyActionOverwrite: ApplyActionOverwrite = UseDefault)

    fun receive(envYamlSource: EnvYamlSource, applyActionOverwrite: ApplyActionOverwrite = UseDefault)
}