package io.kuberig.cluster.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.kuberig.config.KubeRigFlags
import io.kuberig.core.deployment.APIResourceList
import io.kuberig.kubectl.AuthDetails
import kong.unirest.Unirest
import kong.unirest.UnirestInstance
import org.json.JSONObject

class ClusterInteractionService(
        private val apiServerUrl: String,
        flags: KubeRigFlags,
        certificateAuthorityData: String?,
        authDetails: AuthDetails
) {
    private val unirestInstance: UnirestInstance

    init {
        val objectMapper = ObjectMapper()
        objectMapper.findAndRegisterModules()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)

        this.unirestInstance = Unirest.spawnInstance()
        ClusterClientBuilder(flags, objectMapper, unirestInstance)
            .initializeClient(certificateAuthorityData, authDetails)
    }

    private fun kindBaseUrl(kindBasics: KindBasics, namespace: String): String {
        val apiVersion = kindBasics.apiVersion
        val kind = kindBasics.kind
        val kindPlural = kindBasics.kindPlural

        val apiOrApisPart = if (apiVersion == "v1") {
            "api"
        } else {
            "apis"
        }

        return if (kindPlural == null) {
            val getResourceList = unirestInstance.get("$apiServerUrl/$apiOrApisPart/$apiVersion")
            val apiResourceList = getResourceList
                .asObject(APIResourceList::class.java)

            val apiResource = apiResourceList.body.resources.first { it.kind == kind }

            "$apiServerUrl/$apiOrApisPart/$apiVersion/namespaces/$namespace/${apiResource.name}"
        } else {
            "$apiServerUrl/$apiOrApisPart/$apiVersion/namespaces/$namespace/$kindPlural"
        }
    }

    fun resourceGetUrl(kindBasics: KindBasics, namespace: String, resourceName: String): String {
        val kindBaseUrl = kindBaseUrl(kindBasics, namespace)

        return "$kindBaseUrl/$resourceName"
    }

    fun resourcePostUrl(kindBasics: KindBasics, namespace: String): String {
        return kindBaseUrl(kindBasics, namespace)
    }

    fun read(kindBasics: KindBasics, namespace: String, resourceName: String): ResourceReadResult {
        val resourceUrl = this.resourceGetUrl(kindBasics, namespace, resourceName)

        val getResult = unirestInstance.get(resourceUrl)
            .asJson()

        return if (getResult.status == 200) {
            FoundResourceReadResult(resourceUrl, getResult.body.`object`)
        } else {
            NotFoundResourceReadResult(resourceUrl)
        }
    }

    fun create(kindBasics: KindBasics, namespace: String, resourceBody: JSONObject): ResourceCreateResult {
        val resourcePostUrl = this.resourcePostUrl(kindBasics, namespace)

        val postResult = unirestInstance.post(resourcePostUrl)
            .header("Content-Type", "application/json")
            .body(resourceBody)
            .asJson()

        return if (postResult.status == 201) {
            ResourceCreateSuccessResult(postResult.body.`object`)
        } else {
            ResourceCreateFailedResult(postResult.status, postResult.statusText, postResult.body.`object`)
        }
    }

    fun shutdown() {
        unirestInstance.shutDown()
    }
}

class KindBasics(val apiVersion: String, val kind: String, val kindPlural: String? = null) {

    fun startObject(namespace: String, resourceName: String): JSONObject {
        return JSONObject()
            .put("apiVersion", apiVersion)
            .put("kind", kind)
            .put(
                "metadata", JSONObject()
                    .put("namespace", namespace)
                    .put("name", resourceName)
            )
    }

}

sealed class ResourceReadResult(val resourceUrl: String)
class NotFoundResourceReadResult(resourceUrl: String) : ResourceReadResult(resourceUrl)
class FoundResourceReadResult(resourceUrl: String, val resourceBody: JSONObject) : ResourceReadResult(resourceUrl)

sealed class ResourceCreateResult
class ResourceCreateSuccessResult(val body: JSONObject) : ResourceCreateResult()
class ResourceCreateFailedResult(val status: Int, val statusText: String, val body: JSONObject) : ResourceCreateResult()