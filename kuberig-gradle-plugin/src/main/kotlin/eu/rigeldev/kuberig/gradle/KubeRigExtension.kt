package eu.rigeldev.kuberig.gradle

open class KubeRigExtension {

    var targetPlatform = TargetPlatform(PlatformType.KUBERNETES, KubernetesVersion.V1_12_8)

    fun kubernetes(version : KubernetesVersion) {
        this.targetPlatform = TargetPlatform(PlatformType.KUBERNETES, version)
    }

    fun openshift(version : OpenshiftVersion) {
        this.targetPlatform = TargetPlatform(PlatformType.OPENSHIFT, version)
    }

}