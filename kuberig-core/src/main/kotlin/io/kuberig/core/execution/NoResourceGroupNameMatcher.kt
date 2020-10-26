package io.kuberig.core.execution

class NoResourceGroupNameMatcher : ResourceGroupNameMatcher {
    override fun matchResourceGroupName(definedName: String?): Boolean {
        return definedName == null
    }
}