package eu.rigeldev.kuberig.gradle.config

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.config.KubeRigFlags
import eu.rigeldev.kuberig.core.deploy.control.DeployControl
import eu.rigeldev.kuberig.encryption.EncryptionSupportFactory
import eu.rigeldev.kuberig.encryption.tink.TinkEncryptionSupportFactory
import eu.rigeldev.kuberig.fs.MethodNameOutputFileConvention
import eu.rigeldev.kuberig.fs.OutputFileConvention
import eu.rigeldev.kuberig.fs.RootFileSystem
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

open class KubeRigExtension(private val project : Project) {

    var flags : KubeRigFlags = KubeRigFlags()

    var targetPlatform : TargetPlatform = TargetPlatform(PlatformType.KUBERNETES, "v1.14.2")
    val environments : NamedDomainObjectContainer<KubeRigEnvironment> = this.project.container(
        KubeRigEnvironment::class.java)

    var encryptionSupportFactoryType: Class<out EncryptionSupportFactory>? = TinkEncryptionSupportFactory::class.java

    var yamlOutputFileConvention: OutputFileConvention = MethodNameOutputFileConvention()

    private var deployControl = DeployControl()

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
        return RootFileSystem(project.rootDir, this.encryptionSupportFactory())
    }
}