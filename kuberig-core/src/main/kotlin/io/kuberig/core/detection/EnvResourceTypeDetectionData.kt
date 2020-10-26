package io.kuberig.core.detection

data class EnvResourceTypeDetectionData(var isAbstract : Boolean,
                                        var className : String,
                                        var superClassName : String,
                                        var resourceMethods : List<String>)