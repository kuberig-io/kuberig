package io.kuberig.annotations

/**
 * Marker annotation for methods that generate multiple resources.
 * The generated resources are published by calling one or multiple emit functions on DslResourceEmitter.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EnvResources(val group: String = "")