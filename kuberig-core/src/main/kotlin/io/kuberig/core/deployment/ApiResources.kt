package io.kuberig.core.deployment

import io.kuberig.core.resource.RawResourceInfo

class ApiResources(
    apiServerIntegration: ApiServerIntegration,
    apiServerUrl: String,
    apiVersion: String) {

    private val apiVersionBaseUrl = ApiServerUrlSupport.apiVersionBaseUrl(apiServerUrl, apiVersion)
    private val apiResourceList = apiServerIntegration.retrieveApiResources(apiVersionBaseUrl)

    private fun apiResource(kind: String): APIResource {
        return apiResourceList.first { it.kind == kind }
    }

    fun resourceUrl(rawResourceInfo: RawResourceInfo): ResourceUrlInfo {
        val apiResource = this.apiResource(rawResourceInfo.kind)

        val resourceTypeUrl = ApiServerUrlSupport.resourceTypeUrl(
            apiVersionBaseUrl,
            rawResourceInfo.namespace,
            apiResource.name
        )

        val resourceUrl = ApiServerUrlSupport.resourceUrl(
            resourceTypeUrl,
            rawResourceInfo.resourceName
        )

        return ResourceUrlInfo(resourceTypeUrl, resourceUrl)
    }
}

data class ResourceUrlInfo(val resourceTypeUrl: String,
                           val resourceUrl: String)