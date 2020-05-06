package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.dsl.KubernetesResourceDslType
import eu.rigeldev.kuberig.dsl.model.BasicResource
import eu.rigeldev.kuberig.dsl.model.FullResource
import eu.rigeldev.kuberig.dsl.support.DslResourceEmitter
import eu.rigeldev.kuberig.dsl.support.DslResourceReceiver

class ResourceEmittingMethodCallContextProcessor : MethodCallContextProcessor {

    override fun process(methodCallContext: MethodCallContext, resources: MutableList<FullResource>) {

        DslResourceEmitter.registerReceiver(object : DslResourceReceiver {
            override fun getName(): String {
                return "default-receiver"
            }

            override fun <T> receive(dslType: DslType<T>) {
                resources.add(dslType.toValue() as Any)
            }
        })

        methodCallContext.method.invoke(methodCallContext.typeInstance)
    }
}