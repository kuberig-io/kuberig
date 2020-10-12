package eu.rigeldev.kuberig.gradle.config

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.config.KubeRigFlags
import eu.rigeldev.kuberig.core.deploy.control.DeployControl
import eu.rigeldev.kuberig.encryption.EncryptionSupportFactory
import eu.rigeldev.kuberig.encryption.tink.TinkEncryptionSupportFactory
import eu.rigeldev.kuberig.fs.NameKindOutputFileConvention
import eu.rigeldev.kuberig.fs.OutputFileConvention
import eu.rigeldev.kuberig.fs.RootFileSystem
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import java.util.*

open class KubeRigExtension(private val project : Project) {

    var flags : KubeRigFlags = KubeRigFlags()

    var targetPlatform : TargetPlatform = TargetPlatform(PlatformType.KUBERNETES, "v1.14.2")

    /**
     * Allows using a custom KubeRig DSL.
     */
    var apiDslDependencyOverride: String? = null

    val environments : NamedDomainObjectContainer<KubeRigEnvironment> = this.project.container(
        KubeRigEnvironment::class.java)

    var encryptionSupportFactoryType: Class<out EncryptionSupportFactory>? = TinkEncryptionSupportFactory::class.java

    var yamlOutputFileConvention: OutputFileConvention = NameKindOutputFileConvention()

    private var deployControl = DeployControl()

    private var dependencyVersions: DependencyVersions

    /**
     * From KubeRig version 0.0.42 the minimal KubeRig-DSL version is 0.0.21 as there is a hard dependency on
     * the new eu.rigeldev.kuberig.dsl.KubernetesResourceDslType and eu.rigeldev.kuberig.dsl.model package.
     */
    private val minimalKuberigDslVersion = SemVersion(0, 0, 21)

    init {
        val props = this.loadProps()

        this.dependencyVersions = DependencyVersions(
            SemVersion.fromVersionText(props["kuberig.version"] as String),
            SemVersion.fromVersionText(props["kuberig.dsl.version"] as String),
            SemVersion.fromVersionText(props["kotlin.version"] as String)
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


    fun deployControl(init: DeployControl.() -> Unit) {
        this.deployControl.init()
    }

    fun getDeployControl(): DeployControl {
        return this.deployControl
    }

    fun encryptionSupportFactory(): EncryptionSupportFactory {
        return this.encryptionSupportFactoryType!!.getConstructor().newInstance()
    }

    fun rootFileSystem(): RootFileSystem {
        return RootFileSystem(project.rootDir, project.projectDir, this.encryptionSupportFactory())
    }

    /**
     * By default the version of the kuberig-dsl-xxx dependencies is determined by the version configured in the
     * packaged kuberig.properties file.
     *
     * This method provides a way to overwrite the version of the kuberig-dsl-xxx that is used.
     */
    fun kuberigDslVersion(kuberigDslVersion: String) {
        val parsedKuberigDslVersion = SemVersion.fromVersionText(kuberigDslVersion)

        check(parsedKuberigDslVersion.isEqual(minimalKuberigDslVersion) || parsedKuberigDslVersion.isHigher(minimalKuberigDslVersion)) {
            "KubeRig version ${this.dependencyVersions.kuberigVersion} requires the KubeRig-DSL version to be $minimalKuberigDslVersion or higher; as there is a hard dependency on\n" +
                    " the new eu.rigeldev.kuberig.dsl.KubernetesResourceDslType and eu.rigeldev.kuberig.dsl.model package. "
        }

        this.dependencyVersions = DependencyVersions(
            this.dependencyVersions.kuberigVersion,
            parsedKuberigDslVersion,
            this.dependencyVersions.kotlinVersion
        )
    }

    /**
     * By default the version of the kotlin-stdlib-jdk8 dependency is determined by the version configured in the
     * packaged kuberig.properties file.
     *
     * This method provides a way to overwrite the version of the kotlin-stdlib-jdk8 that is used.
     */
    fun kotlinVersion(kotlinVersion: String) {
        this.dependencyVersions = DependencyVersions(
            this.dependencyVersions.kuberigVersion,
            this.dependencyVersions.kuberigDslVersion,
            SemVersion.fromVersionText(kotlinVersion)
        )
    }

    fun kuberigDslVersion(): String {
        return this.dependencyVersions.kuberigDslVersion.toString()
    }

    fun kuberigVersion(): String {
        return this.dependencyVersions.kuberigVersion.toString()
    }

    fun kotlinVersion(): String {
        return this.dependencyVersions.kotlinVersion.toString()
    }

    private fun loadProps(): Properties {
        val props = Properties()
        props.load(this.javaClass.getResourceAsStream("/kuberig.properties"))
        return props
    }
}
