package io.kuberig.dsl.support

import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.BasicResource

interface DslResourceReceiver {

    fun getName() : String

    fun <T : BasicResource> receive(dslType: KubernetesResourceDslType<T>)

}