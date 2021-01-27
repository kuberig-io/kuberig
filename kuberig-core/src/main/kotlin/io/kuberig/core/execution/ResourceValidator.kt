package io.kuberig.core.execution

import io.kuberig.dsl.KubernetesResourceDslType
import io.kuberig.dsl.model.BasicResource
import io.kuberig.dsl.model.FullResource
import org.slf4j.LoggerFactory

object ResourceValidator {
    private val logger = LoggerFactory.getLogger(ResourceValidator::class.java)

    fun <T : BasicResource> isValidResource(
        dslType: KubernetesResourceDslType<T>,
        resource: T,
        sourceLocation: String
    ): Boolean {
        val resourcePackageName = dslType::class.java.packageName

        val packageCorrect = resourcePackageName.startsWith("kinds.")
        val fullResource = FullResource::class.java.isAssignableFrom(resource::class.java)

        if (!packageCorrect) {
            logger.error(dslType::class.java.name + " is not within the kinds.* package, skipping. [${sourceLocation}]")
        }

        if (!fullResource) {
            logger.error(dslType::class.java.name + " does not generate a ${FullResource::class.java}, skipping [${sourceLocation}]")
        }

        return packageCorrect && fullResource
    }

}