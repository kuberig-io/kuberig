package io.kuberig.core.generation.yaml

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.util.*

class ByteArraySerializer : StdSerializer<ByteArray>(ByteArray::class.java) {
    override fun serialize(value: ByteArray?, gen: JsonGenerator?, provider: SerializerProvider?) {
        val encodedValue = Base64.getEncoder().encodeToString(value)
        gen!!.writeString(encodedValue)
    }
}