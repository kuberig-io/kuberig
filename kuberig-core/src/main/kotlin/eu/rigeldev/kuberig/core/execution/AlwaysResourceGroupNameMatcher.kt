package eu.rigeldev.kuberig.core.execution

class AlwaysResourceGroupNameMatcher : ResourceGroupNameMatcher {
    override fun matchResourceGroupName(definedName: String?): Boolean {
        return true
    }
}