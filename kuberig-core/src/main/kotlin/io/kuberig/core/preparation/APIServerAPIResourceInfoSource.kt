package io.kuberig.core.preparation

import io.kuberig.core.deployment.ApiServerIntegration
import io.kuberig.core.deployment.ApiServerUrlSupport

class APIServerAPIResourceInfoSource(
    private val apiServerIntegration: ApiServerIntegration,
    private val apiServerUrl: String
) : APIResourceInfoSource {

    override fun apiResource(apiVersion: String, kind: String): APIResourceInfo {
        val apiVersionBaseUrl = ApiServerUrlSupport.apiVersionBaseUrl(apiServerUrl, apiVersion)
        val apiResourceList = apiServerIntegration.retrieveApiResources(apiVersionBaseUrl)

        val apiResource = apiResourceList.find { it.kind == kind }

        return if (apiResource == null) {
            UnknownAPIResourceInfo
        } else {
            KnownAPIResourceInfo(apiResource)
        }
    }
}

