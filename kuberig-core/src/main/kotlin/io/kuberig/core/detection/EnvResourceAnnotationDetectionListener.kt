package io.kuberig.core.detection

interface EnvResourceAnnotationDetectionListener {

    fun receiveEnvResourceAnnotatedType(className: String, annotatedMethods: Set<String>)
}