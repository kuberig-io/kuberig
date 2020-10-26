package io.kuberig.annotations

/**
 * Provides control over when an @EnvResource is applied during deploy.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Tick (val tick: Int)