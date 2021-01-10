package io.kuberig.gradle.config

import io.kuberig.config.ClientSideApplyFlags
import io.kuberig.config.KubeRigEnvironment
import io.kuberig.config.KubeRigFlags
import io.kuberig.config.ServerSideApplyFlags
import io.kuberig.core.deployment.control.TickInfo
import io.kuberig.encryption.EncryptionSupportFactory
import io.kuberig.encryption.tink.TinkEncryptionSupportFactory
import io.kuberig.fs.NameKindOutputFileConvention
import io.kuberig.fs.OutputFileConvention
import io.kuberig.fs.RootFileSystem
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import java.lang.IllegalStateException
import java.util.*

open class KubeRigExtension(private val project : Project) {

    var flags : KubeRigFlags = KubeRigFlags()

    var targetPlatform : TargetPlatform = TargetPlatform(PlatformType.KUBERNETES, "v1.19.4")

    /**
     * Allows using a custom KubeRig DSL.
     */
    var dslDependencyOverride: String? = null

    val environments : NamedDomainObjectContainer<KubeRigEnvironment> = this.project.container(KubeRigEnvironment::class.java)

    var encryptionSupportFactoryType: Class<out EncryptionSupportFactory>? = TinkEncryptionSupportFactory::class.java

    var yamlOutputFileConvention: OutputFileConvention = NameKindOutputFileConvention()

    private var tickInfo = TickInfo()

    private var dependencyVersions: DependencyVersions

    /**
     * From KubeRig version 0.0.43 the minimal KubeRig-DSL version is 0.1.1 as there is a hard dependency on
     * the new io.kuberig.dsl.KubernetesResourceDslType and io.kuberig.dsl.model package.
     */
    private val minimalKuberigDslVersion = SemVersion(0, 1, 1)

    init {
        val props = this.loadProps()

        this.dependencyVersions = DependencyVersions(
                props["kuberig.version"] as String,
                props["kuberig.dsl.version"] as String,
                props["kotlin.version"] as String
        )
    }

    /**
     * Switch to Kubernetes as target platform.
     *
     * @param version The platform version. Should be equal to a tag of the kubernetes/kubernetes github repository.
     */
    fun kubernetes(version : String) {
        this.targetPlatform = TargetPlatform(PlatformType.KUBERNETES, version)
    }

    /**
     * Switch to Openshift as target platform.
     *
     * @param version The platform version. Should be equal to a tag of the openshift/origin github repository.
     */
    fun openshift(version : String) {
        this.targetPlatform = TargetPlatform(PlatformType.OPENSHIFT, version)
    }


    fun tickInfo(init: TickInfo.() -> Unit) {
        this.tickInfo.init()
    }

    fun getTickInfo(): TickInfo {
        return this.tickInfo
    }

    fun encryptionSupportFactory(): EncryptionSupportFactory {
        return this.encryptionSupportFactoryType!!.getConstructor().newInstance()
    }

    fun rootFileSystem(): RootFileSystem {
        return RootFileSystem(project.rootDir, project.projectDir, this.encryptionSupportFactory())
    }

    /**
     * By default the version of the kuberig-dsl-xxx dependencies is determined by the version configured in the
     * packaged io.kuberig.kuberig.properties file.
     *
     * This method provides a way to overwrite the version of the kuberig-dsl-xxx that is used.
     */
    fun kuberigDslVersion(kuberigDslVersion: String) {
        val parsedKuberigDslVersion = parseSemVersion(kuberigDslVersion)
        if (parsedKuberigDslVersion != null) {
            check(
                parsedKuberigDslVersion.isEqual(minimalKuberigDslVersion) || parsedKuberigDslVersion.isHigher(
                    minimalKuberigDslVersion
                )
            ) {
                "KubeRig version ${this.dependencyVersions.kuberigVersion} requires the KubeRig-DSL version to be $minimalKuberigDslVersion or higher; as there is a hard dependency on\n" +
                        " the new io.kuberig.dsl.KubernetesResourceDslType and io.kuberig.dsl.model package. "
            }
        } else {
            project.logger.warn("Provided Kuberig dsl version is not a sem version unable to determine if minimum version of $minimalKuberigDslVersion is met.")
        }

        this.dependencyVersions = DependencyVersions(
            this.dependencyVersions.kuberigVersion,
            kuberigDslVersion,
            this.dependencyVersions.kotlinVersion
        )
    }

    private fun parseSemVersion(versionText: String): SemVersion? {
        return try {
            SemVersion.fromVersionText(versionText)
        }
        catch (e: IllegalStateException) {
            null
        }
    }

    /**
     * By default the version of the kotlin-stdlib-jdk8 dependency is determined by the version configured in the
     * packaged io.kuberig.kuberig.properties file.
     *
     * This method provides a way to overwrite the version of the kotlin-stdlib-jdk8 that is used.
     */
    fun kotlinVersion(kotlinVersion: String) {
        this.dependencyVersions = DependencyVersions(
            this.dependencyVersions.kuberigVersion,
            this.dependencyVersions.kuberigDslVersion,
            kotlinVersion
        )
    }

    fun kuberigDslVersion(): String {
        return this.dependencyVersions.kuberigDslVersion
    }

    fun kuberigVersion(): String {
        return this.dependencyVersions.kuberigVersion
    }

    fun kotlinVersion(): String {
        return this.dependencyVersions.kotlinVersion
    }

    private fun loadProps(): Properties {
        val props = Properties()
        props.load(this.javaClass.getResourceAsStream("/io.kuberig.kuberig.properties"))
        return props
    }

    fun serverSideApply(init: ServerSideApplyFlags.() -> Unit) {
        this.flags.serverSideApply(init)
    }

    fun clientSideApply(init: ClientSideApplyFlags.() -> Unit) {
        this.flags.clientSideApply(init)
    }
}
