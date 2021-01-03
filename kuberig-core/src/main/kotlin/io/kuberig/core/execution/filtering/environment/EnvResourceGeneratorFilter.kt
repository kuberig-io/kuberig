package io.kuberig.core.execution.filtering.environment

import io.kuberig.config.KubeRigEnvironment
import io.kuberig.annotations.EnvFilter
import io.kuberig.core.execution.filtering.ResourceGeneratorFilter
import java.lang.reflect.Method

/**
 * Applies the EnvFilter annotation.
 */
class EnvResourceGeneratorFilter(val environment: KubeRigEnvironment) : ResourceGeneratorFilter {

    override fun shouldGenerate(method: Method): Boolean {
        val envFilterAnnotation = method.getDeclaredAnnotation(EnvFilter::class.java)

        return if (envFilterAnnotation != null) {
            val currentEnvironmentName = environment.name

            envFilterAnnotation.environments.map(String::toLowerCase).contains(currentEnvironmentName.toLowerCase())
        } else {
            true
        }
    }
}