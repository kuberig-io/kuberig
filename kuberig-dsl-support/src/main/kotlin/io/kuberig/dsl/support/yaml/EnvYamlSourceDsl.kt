package io.kuberig.dsl.support.yaml

import io.kuberig.dsl.KubeRigDslMarker
import io.kuberig.dsl.support.*
import java.io.File
import java.io.InputStream

@KubeRigDslMarker
class EnvYamlSourceDsl {

    private var targetNamespace : TargetNamespace = UseSourceOrEnvironmentDefault
    private lateinit var location: String
    private lateinit var streamSupplier: StreamSupplier

    /**
     * If the YAML source has a namespace it will be used.
     * If the YAML does not specify a namespace for a namespaced resource it will use the default namespace
     * for the environment.
     */
    fun useSourceOrEnvironmentDefault() {
        this.targetNamespace = UseSourceOrEnvironmentDefault
    }

    /**
     * Only use the namespace as defined by the YAML source.
     * If a namespace is missing for a namespaced resource this will result in an error.
     */
    fun useSourceNamespace() {
        this.targetNamespace = UseSource
    }

    /**
     * Always use the default namespace for the environment for namespaced resources.
     */
    fun useEnvironmentDefaultNamespace() {
        this.targetNamespace = UseEnvironmentDefault
    }

    /**
     * Always use the provided namespace for namespaced resources.
     */
    fun useSpecificNamespace(namespace: String) {
        this.targetNamespace = UseSpecific(namespace)
    }

    fun from(location: String) {
        this.location = location
        this.streamSupplier = RepositoryLocationStreamSupplier { repositoryRootDirectory ->
            File(repositoryRootDirectory, location).inputStream()
        }
    }

    fun from(location: String, streamSupplier: (String) -> InputStream) {
        this.location = location
        this.streamSupplier = GenericStreamSupplier(streamSupplier)
    }

    fun toValue(): EnvYamlSource {
        return EnvYamlSource(location, streamSupplier, targetNamespace)
    }
}

/**
 * Define an environment YAML file.
 * The location is used relative to the project root.
 */
fun envYamlSource(init: EnvYamlSourceDsl.() -> Unit): EnvYamlSourceDsl {
    val dsl = EnvYamlSourceDsl()
    dsl.init()
    return dsl
}