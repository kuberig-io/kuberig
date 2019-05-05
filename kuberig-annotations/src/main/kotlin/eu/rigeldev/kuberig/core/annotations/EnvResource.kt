package eu.rigeldev.kuberig.core.annotations

/**
 * Marker annotation for methods that generate an environment resource.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EnvResource