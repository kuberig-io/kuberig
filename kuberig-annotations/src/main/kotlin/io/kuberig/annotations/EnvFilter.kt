package io.kuberig.annotations

/**
 * Annotation to limit the environments for an environment resource.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EnvFilter(
    /**
     * The environments the environment resource should be included.
     *
     * Matched in a case insensitive way.
     */
    val environments: Array<String>
)