package eu.rigeldev.kuberig.gradle

enum class KubernetesVersion(private val versionText : String) : PlatformVersion {

    V1_12_8("v1.12.8"),

    V1_15_0_ALPHA_1("v1.15.0-alpha.1");

    override fun versionText(): String {
        return this.versionText
    }
}
