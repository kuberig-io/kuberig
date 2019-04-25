package eu.rigeldev.kuberig.core.annotations

/**
 * Marker annotation for methods that generate a resource.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class KubeRigResource