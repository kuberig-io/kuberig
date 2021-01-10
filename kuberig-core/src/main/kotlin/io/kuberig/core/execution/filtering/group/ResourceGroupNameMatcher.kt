package io.kuberig.core.execution.filtering.group

interface ResourceGroupNameMatcher {

    fun matchResourceGroupName(definedName: String?): Boolean

}