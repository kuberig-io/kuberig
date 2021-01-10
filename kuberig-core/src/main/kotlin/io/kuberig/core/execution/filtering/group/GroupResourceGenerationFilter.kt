package io.kuberig.core.execution.filtering.group

import io.kuberig.annotations.EnvResource
import io.kuberig.annotations.EnvResources
import io.kuberig.core.execution.filtering.ResourceGeneratorFilter
import java.lang.reflect.Method

class GroupResourceGenerationFilter(private val groupNameMatcher: ResourceGroupNameMatcher) : ResourceGeneratorFilter {

    override fun shouldGenerate(method: Method): Boolean {
        val envResourceAnnotation = method.getDeclaredAnnotation(EnvResource::class.java)
        val envResourcesAnnotation = method.getDeclaredAnnotation(EnvResources::class.java)

        val definedResourceGroupName : String? = if (envResourceAnnotation != null) {
            if (envResourceAnnotation.group == "") {
                null
            } else {
                envResourceAnnotation.group
            }
        } else {
            if (envResourcesAnnotation.group == "") {
                null
            } else {
                envResourcesAnnotation.group
            }
        }

        return groupNameMatcher.matchResourceGroupName(definedResourceGroupName)
    }
}