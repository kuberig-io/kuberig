package io.kuberig.core.detection

import io.kuberig.core.model.GeneratorMethod

data class EnvResourceTypeDetectionData(var isAbstract : Boolean,
                                        var className : String,
                                        var superClassName : String,
                                        var resourceMethods : List<GeneratorMethod>)