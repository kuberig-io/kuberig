package io.kuberig.core.detection

import io.kuberig.core.model.GeneratorType

interface GeneratorTypeConsumer {

    fun consume(generatorType: GeneratorType)
}