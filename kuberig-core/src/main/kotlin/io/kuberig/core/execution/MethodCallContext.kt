package io.kuberig.core.execution

import java.lang.reflect.Method

class MethodCallContext(val type: Class<*>,
                        val typeInstance: Any,
                        val method: Method,
                        val methodType: ResourceGenerationMethodType)