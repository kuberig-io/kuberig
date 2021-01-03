package io.kuberig.dsl.support

import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.BasicResource
import io.kuberig.dsl.model.FullResource

interface DslResourceReceiver {

    fun getName() : String

    fun receive(dslType: KubernetesResourceDslType<FullResource>, applyActionOverwrite: ApplyActionOverwrite = UseDefault)

}