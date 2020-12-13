package io.kuberig.core.deployment

class ApiResources(
    apiServerIntegration: ApiServerIntegration,
    apiServerUrl: String,
    apiVersion: String) {

    private val apiVersionBaseUrl = ApiServerUrlSupport.apiVersionBaseUrl(apiServerUrl, apiVersion)
    private val apiResourceList = apiServerIntegration.retrieveApiResources(apiVersionBaseUrl)

    private fun apiResource(kind: String): APIResource {
        return apiResourceList.first { it.kind == kind }
    }

    fun resourceUrl(newResourceInfo: NewResourceInfo): ResourceUrlInfo {
        val apiResource = this.apiResource(newResourceInfo.kind)

        val resourceTypeUrl = ApiServerUrlSupport.resourceTypeUrl(
            apiVersionBaseUrl,
            newResourceInfo.namespace,
            apiResource.name
        )

        val resourceUrl = ApiServerUrlSupport.resourceUrl(
            resourceTypeUrl,
            newResourceInfo.resourceName
        )

        return ResourceUrlInfo(resourceTypeUrl, resourceUrl)
    }
}

data class ResourceUrlInfo(val resourceTypeUrl: String,
                           val resourceUrl: String)