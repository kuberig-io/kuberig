package eu.rigeldev.kuberig.gradle

enum class OpenshiftVersion(private val versionText : String) : PlatformVersion {
    V3_6_0("v3.6.0");

    override fun versionText(): String {
        return this.versionText
    }
}