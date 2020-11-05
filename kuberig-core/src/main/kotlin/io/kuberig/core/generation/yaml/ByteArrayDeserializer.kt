package io.kuberig.core.generation.yaml

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.util.*

class ByteArrayDeserializer : StdDeserializer<ByteArray>(ByteArray::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): ByteArray {
        val encodedValue = p!!.readValueAs(String::class.java)
        return Base64.getDecoder().decode(encodedValue)
    }
}