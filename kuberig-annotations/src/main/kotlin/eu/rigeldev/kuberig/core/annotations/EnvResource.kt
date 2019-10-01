package eu.rigeldev.kuberig.core.annotations

/**
 * Marker annotation for methods that generate a single environment resource.
 * The generated resource is published as return value from the method.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EnvResource