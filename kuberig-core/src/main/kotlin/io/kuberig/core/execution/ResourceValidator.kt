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
        userCallLocationProvider: () -> String?
    ): Boolean {
        val resourcePackageName = dslType::class.java.packageName

        val packageCorrect = resourcePackageName.startsWith("kinds.")
        val fullResource = FullResource::class.java.isAssignableFrom(resource::class.java)

        if (!packageCorrect) {
            val userCallLocation: String? = userCallLocationProvider()

            logger.error(dslType::class.java.name + " is not within the kinds.* package, skipping. [${userCallLocation}]")

        }

        if (!fullResource) {
            val userCallLocation: String? = userCallLocationProvider()

            logger.error(dslType::class.java.name + " does not generate a ${FullResource::class.java}, skipping [${userCallLocation}]")
        }

        return packageCorrect && fullResource
    }

}