package io.kuberig.core.deployment

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.kuberig.cluster.client.ClusterClientBuilder
import io.kuberig.config.KubeRigFlags
import io.kuberig.kubectl.AuthDetails
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import kong.unirest.UnirestInstance
import org.json.JSONObject

class ApiServerIntegration(
    certificateAuthorityData: String?,
    authDetails: AuthDetails,
    flags: KubeRigFlags
) {
    private val objectMapper = ObjectMapper()
    private val unirestInstance: UnirestInstance

    init {
        objectMapper.findAndRegisterModules()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)

        unirestInstance = Unirest.primaryInstance()

        ClusterClientBuilder(flags, objectMapper, unirestInstance)
            .initializeClient(certificateAuthorityData, authDetails)
    }

    fun getResource(resourceUrlInfo: ResourceUrlInfo): GetResourceResult {
        val getRequest = unirestInstance.get(resourceUrlInfo.resourceUrl)
        val getResult = getRequest.asJson()

        return if (getResult.status == 404) {
            UnknownGetResourceResult(resourceUrlInfo)
        } else {
            val currentJson = getResult.body.`object`
            val resourceVersion = currentJson.getJSONObject("metadata").getString("resourceVersion")
            ExistsGetResourceResult(resourceUrlInfo, currentJson, resourceVersion)
        }
    }

    fun postResource(newResourceInfo: NewResourceInfo, resourceUrlInfo: ResourceUrlInfo): PostResourceResult {
        val postResponse = unirestInstance.post(resourceUrlInfo.resourceTypeUrl)
            .header("Content-Type", "application/json")
            .body(newResourceInfo.json)
            .asString()

        return if (postResponse.status == 201) {
            SuccessPostResourceResult(resourceUrlInfo, newResourceInfo)
        } else {
            FailedPostResourceResult(resourceUrlInfo, newResourceInfo, postResponse)
        }
    }

    fun serverSideApplyResourcePatch(
        newResourceInfo: NewResourceInfo,
        resourceUrlInfo: ResourceUrlInfo,
        force: Boolean
    ): ServerSideApplyPatchResourceResult {
        val patchResponse = unirestInstance.patch(resourceUrlInfo.resourceUrl)
            .header("Content-Type", "application/apply-patch+yaml")
            .accept("application/json")
            .queryString("fieldManager", "kuberig")
            .queryString("force", force)
            .body(newResourceInfo.json)
            .asString()

        return if (patchResponse.status == 200 || patchResponse.status == 201) {
            SuccessServerSideApplyPatchResourceResult(resourceUrlInfo, newResourceInfo)
        } else {
            FailedServerSideApplyPatchResourceResult(resourceUrlInfo, newResourceInfo, patchResponse)
        }
    }

    fun putResource(updateResourceInfo: NewResourceInfo, resourceUrlInfo: ResourceUrlInfo): PutResourceResult {
        val putResponse = unirestInstance.put(resourceUrlInfo.resourceUrl)
            .header("Content-Type", "application/json")
            .body(updateResourceInfo.json)
            .asString()

        return when (putResponse.status) {
            422 -> {
                ConflictPutResourceResult(resourceUrlInfo, updateResourceInfo, putResponse)
            }
            200 -> {
                SuccessPutResourceResult(resourceUrlInfo, updateResourceInfo)
            }
            else -> {
                FailedPutResourceResult(resourceUrlInfo, updateResourceInfo, putResponse)
            }
        }
    }

    fun deleteResource(resourceUrlInfo: ResourceUrlInfo): DeleteResourceResult {
        val deleteResponse = unirestInstance.delete(resourceUrlInfo.resourceUrl)
            .header("Content-Type", "application/json")
            .asString()

        return if (deleteResponse.status == 200) {
            SuccessDeleteResourceResult(resourceUrlInfo)
        } else {
            FailedDeleteResourceResult(resourceUrlInfo, deleteResponse)
        }
    }

    fun jsonSerialize(resource: Any): String {
        return objectMapper.writeValueAsString(resource)
    }

    fun retrieveApiResources(apiVersionBaseUrl: String): List<APIResource> {
        return unirestInstance.get(apiVersionBaseUrl)
            .asObject(APIResourceList::class.java)
            .body
            .resources
    }

    fun shutDown() {
        this.unirestInstance.shutDown()
    }

}

abstract class ResourceResult(val urlInfo: ResourceUrlInfo) {
    abstract fun logInfo(applyStrategy: ApplyStrategy<Any>)

    protected fun showResponseDetails(response: HttpResponse<String>) {
        println(response.status)
        println(response.statusText)

        if (response.headers["Content-Type"][0] == "application/json") {
            val json = JSONObject(response.body)
            println(json.getString("message"))
        } else {
            println(response.body)
        }
    }
}

sealed class GetResourceResult(urlInfo: ResourceUrlInfo) : ResourceResult(urlInfo)
class ExistsGetResourceResult(urlInfo: ResourceUrlInfo, val json: JSONObject, val resourceVersion: String) :
    GetResourceResult(urlInfo) {
    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        println("${urlInfo.resourceUrl} exists")
    }
}

class UnknownGetResourceResult(urlInfo: ResourceUrlInfo) : GetResourceResult(urlInfo) {
    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        println("${urlInfo.resourceUrl} does not exist")
    }
}

sealed class PostResourceResult(urlInfo: ResourceUrlInfo, val newResourceInfo: NewResourceInfo) :
    ResourceResult(urlInfo)

class FailedPostResourceResult(
    urlInfo: ResourceUrlInfo,
    newResourceInfo: NewResourceInfo,
    val response: HttpResponse<String>
) : PostResourceResult(urlInfo, newResourceInfo) {
    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        super.showResponseDetails(this.response)
        println("Failed to post(${applyStrategy.name}) ${newResourceInfo.fullInfoText()}")
    }
}

class SuccessPostResourceResult(urlInfo: ResourceUrlInfo, newResourceInfo: NewResourceInfo) :
    PostResourceResult(urlInfo, newResourceInfo) {
    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        println("post(${applyStrategy.name}) ${newResourceInfo.infoText()} - success")
    }
}

sealed class ServerSideApplyPatchResourceResult(
    urlInfo: ResourceUrlInfo,
    val newResourceInfo: NewResourceInfo
) : ResourceResult(urlInfo)

class FailedServerSideApplyPatchResourceResult(
    urlInfo: ResourceUrlInfo,
    newResourceInfo: NewResourceInfo,
    val response: HttpResponse<String>
) : ServerSideApplyPatchResourceResult(urlInfo, newResourceInfo) {

    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        super.showResponseDetails(this.response)
        println("Failed to patch(${applyStrategy.name}) ${newResourceInfo.fullInfoText()}")
    }
}

class SuccessServerSideApplyPatchResourceResult(
    urlInfo: ResourceUrlInfo,
    newResourceInfo: NewResourceInfo
) : ServerSideApplyPatchResourceResult(urlInfo, newResourceInfo) {

    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        println("patch(${applyStrategy.name}) ${newResourceInfo.infoText()} - success")
    }
}

sealed class PutResourceResult(
    urlInfo: ResourceUrlInfo,
    val updateResourceInfo: NewResourceInfo
) : ResourceResult(urlInfo)

class ConflictPutResourceResult(
    urlInfo: ResourceUrlInfo,
    updateResourceInfo: NewResourceInfo,
    val response: HttpResponse<String>
) : PutResourceResult(urlInfo, updateResourceInfo) {

    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        super.showResponseDetails(this.response)
        println("Failed(conflict) to put(${applyStrategy.name}) ${updateResourceInfo.fullInfoText()}")
    }
}

class FailedPutResourceResult(
    urlInfo: ResourceUrlInfo,
    updateResourceInfo: NewResourceInfo,
    val response: HttpResponse<String>
) : PutResourceResult(urlInfo, updateResourceInfo) {

    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        super.showResponseDetails(this.response)
        println("Failed to put(${applyStrategy.name}) ${updateResourceInfo.fullInfoText()}")
    }
}

class SuccessPutResourceResult(
    urlInfo: ResourceUrlInfo,
    updateResourceInfo: NewResourceInfo
) : PutResourceResult(urlInfo, updateResourceInfo) {

    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        println("put(${applyStrategy.name}) ${updateResourceInfo.infoText()} - success")
    }
}

sealed class DeleteResourceResult(urlInfo: ResourceUrlInfo) : ResourceResult(urlInfo)
class FailedDeleteResourceResult(urlInfo: ResourceUrlInfo, val response: HttpResponse<String>) :
    DeleteResourceResult(urlInfo) {
    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        super.showResponseDetails(this.response)
        println("Failed to delete(${applyStrategy.name}) ${urlInfo.resourceUrl}")
    }
}

class SuccessDeleteResourceResult(urlInfo: ResourceUrlInfo) : DeleteResourceResult(urlInfo) {
    override fun logInfo(applyStrategy: ApplyStrategy<Any>) {
        println("delete(${applyStrategy.name}) ${urlInfo.resourceUrl} - success")
    }
}