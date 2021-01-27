package io.kuberig.dsl.support.yaml

import io.kuberig.dsl.support.TargetNamespace
import java.io.File
import java.io.InputStream

/**
 * Specifies a YAML file location and what the rule is to determine the target namespace.
 */
data class EnvYamlSource(val location: String, val streamSupplier: StreamSupplier, val targetNamespace : TargetNamespace)

sealed class StreamSupplier
data class RepositoryLocationStreamSupplier(val streamSupplier: (File) -> InputStream): StreamSupplier()
data class GenericStreamSupplier(val streamSupplier: (String) -> InputStream): StreamSupplier()