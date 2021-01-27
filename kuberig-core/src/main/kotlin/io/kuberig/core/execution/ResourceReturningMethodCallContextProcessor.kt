package io.kuberig.core.execution

import io.kuberig.core.preparation.InitialResourceInfo
import io.kuberig.core.preparation.InitialResourceInfoFactory
import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.BasicResource
import io.kuberig.dsl.model.FullResource
import io.kuberig.dsl.support.ApplyActionOverwrite
import io.kuberig.dsl.support.UseDefault
import io.kuberig.dsl.support.UseSourceOrEnvironmentDefault

class ResourceReturningMethodCallContextProcessor(
    private val initialResourceInfoFactory: InitialResourceInfoFactory,
    classLoader: ClassLoader
) : AbstractReturningMethodCallContextProcessor<KubernetesResourceDslType<BasicResource>>(classLoader) {

    @Suppress("UNCHECKED_CAST")
    override fun requiredReturnType(): Class<KubernetesResourceDslType<BasicResource>> {
        // TODO(teyckmans) there must be a better way, just not finding it at this moment.
        val x : KubernetesResourceDslType<BasicResource> = object : KubernetesResourceDslType<BasicResource> {
            override fun toValue(): BasicResource {
                return BasicResource("dummy", "0.0.0")
            }
        }
        return x::class.java.superclass as Class<KubernetesResourceDslType<BasicResource>>
    }

    override fun process(
        methodReturnValue: KubernetesResourceDslType<BasicResource>,
        sourceLocation: String,
        processor: (initialResourceInfo: InitialResourceInfo, applyActionOverwrite: ApplyActionOverwrite) -> Unit
    ) {
        val resource = methodReturnValue.toValue()

        if (ResourceValidator.isValidResource(methodReturnValue, resource, sourceLocation)) {
            val initialResourceInfo = initialResourceInfoFactory.create(
                resource as FullResource,
                sourceLocation,
                UseSourceOrEnvironmentDefault
            )

            processor.invoke(initialResourceInfo, UseDefault)
        }
    }
}