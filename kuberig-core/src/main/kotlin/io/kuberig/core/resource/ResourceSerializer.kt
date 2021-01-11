package io.kuberig.core.resource

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper

object ResourceSerializer {

    private val objectMapper = ObjectMapper()
    init {
        objectMapper.findAndRegisterModules()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)
    }

    fun writeValueAsString(value: Any?): String {
        return objectMapper.writeValueAsString(value)
    }

    fun <T : Any?> readValue(value: String?, valueType: Class<T>?): T {
        return objectMapper.readValue(value, valueType)
    }
}