package io.kuberig.core.execution

interface ResourceGroupNameMatcher {

    fun matchResourceGroupName(definedName: String?): Boolean

}