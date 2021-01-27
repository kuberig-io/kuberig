package io.kuberig.core.preparation

import io.kuberig.core.resource.ResourceSerializer
import io.kuberig.dsl.model.FullResource
import io.kuberig.dsl.support.TargetNamespace
import org.json.JSONObject
import org.json.JSONTokener

class InitialResourceInfoFactory {

    fun create(
        resource: FullResource,
        sourceLocation: String,
        targetNamespace: TargetNamespace
    ): InitialResourceInfo {
        val initialJsonText = ResourceSerializer.writeValueAsString(resource)

        return create(initialJsonText, sourceLocation, targetNamespace)
    }

    fun create(
        initialJsonText: String,
        sourceLocation: String,
        targetNamespace: TargetNamespace
    ): InitialResourceInfo {
        val initialJson = JSONObject(JSONTokener(initialJsonText))

        val apiVersion = initialJson.getString("apiVersion").toLowerCase()
        val kind = initialJson.getString("kind")
        val metadataJson: JSONObject = initialJson.getJSONObject("metadata")
        val resourceName = metadataJson.getString("name")

        return InitialResourceInfo(
            apiVersion,
            kind,
            resourceName,
            sourceLocation,
            targetNamespace,
            initialJson
        )
    }
}