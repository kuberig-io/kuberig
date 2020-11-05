package io.kuberig.core.execution

import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.BasicResource
import io.kuberig.dsl.model.FullResource
import io.kuberig.dsl.support.DslResourceEmitter
import io.kuberig.dsl.support.DslResourceReceiver

class ResourceEmittingMethodCallContextProcessor : MethodCallContextProcessor {

    override fun process(methodCallContext: MethodCallContext, resources: MutableList<FullResource>) {

        DslResourceEmitter.registerReceiver(object : DslResourceReceiver {
            override fun getName(): String {
                return "default-receiver"
            }

            override fun <T: BasicResource> receive(dslType: KubernetesResourceDslType<T>) {
                val resource = dslType.toValue()

                MethodCallContextProcessorResourceFilter.filteringAdd(
                        dslType,
                        resource,
                        resources,
                        {
                            val e = IllegalStateException("")

                            var userCallLocation: String? = null
                            val stackIterator = e.stackTrace.iterator()

                            while (userCallLocation == null && stackIterator.hasNext()) {
                                val stackTraceElement = stackIterator.next()

                                if (stackTraceElement.className == methodCallContext.type.name) {
                                    userCallLocation = stackTraceElement.toString()
                                }
                            }

                            userCallLocation
                        }
                )
            }
        })

        methodCallContext.method.invoke(methodCallContext.typeInstance)
    }
}