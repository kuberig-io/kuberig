package eu.rigeldev.kuberig.dsl.support

import eu.rigeldev.kuberig.dsl.KubernetesResourceDslType
import eu.rigeldev.kuberig.dsl.model.BasicResource

interface DslResourceReceiver {

    fun getName() : String

    fun <T : BasicResource> receive(dslType: KubernetesResourceDslType<T>)

}