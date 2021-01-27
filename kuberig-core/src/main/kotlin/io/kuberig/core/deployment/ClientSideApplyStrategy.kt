package io.kuberig.core.deployment

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import io.kuberig.config.ClientSideApplyFlags
import io.kuberig.core.preparation.ResourceUrlInfo
import io.kuberig.core.resource.RawJsonResourceInfo
import org.json.JSONObject

/**
 * Creates/updates resources using client-side-apply.
 *
 * A limited conflict resolution strategy is implemented that is not completely effective.
 *
 * Use the serverSideApply{} strategy if you can, it the default and more stable option.
 * Client side apply is not on-par with Kubectl and will not be improved upon.
 */
@Deprecated(
    "Kept for backwards compatibility only. " +
            "Use the serverSideApply{} strategy if you can, it the default and more stable option." +
            "Client side apply is not on-par with Kubectl and will not be improved upon. "
)
class ClientSideApplyStrategy(
    apiServerIntegration: ApiServerIntegration,
    flags: ClientSideApplyFlags,
    deploymentListener: DeploymentListener
) : ApplyStrategy<ClientSideApplyFlags>(
    "client-side-apply",
    apiServerIntegration,
    flags,
    deploymentListener
) {

    override fun createResource(rawJsonResourceInfo: RawJsonResourceInfo, resourceUrlInfo: ResourceUrlInfo): Boolean {
        val postResult = apiServerIntegration.postResource(rawJsonResourceInfo, resourceUrlInfo)

        return when (postResult) {
            is FailedPostResourceResult -> {
                deploymentListener.deploymentFailure(rawJsonResourceInfo, postResult)
                false
            }
            is SuccessPostResourceResult -> {
                deploymentListener.deploymentSuccess(rawJsonResourceInfo, postResult)
                true
            }
        }
    }

    override fun updateResource(
        rawJsonResourceInfo: RawJsonResourceInfo,
        resourceUrlInfo: ResourceUrlInfo,
        getResourceResult: ExistsGetResourceResult
    ): Boolean {
        val updateJson = JSONObject(rawJsonResourceInfo.json.toString())
        updateJson.getJSONObject("metadata")
            .put("resourceVersion", getResourceResult.resourceVersion)

        val updateResourceInfo = RawJsonResourceInfo(rawJsonResourceInfo, updateJson)

        return when (val putResult = apiServerIntegration.putResource(updateResourceInfo, resourceUrlInfo)) {
            is ConflictPutResourceResult -> {
                attemptConflictResolution(
                    rawJsonResourceInfo,
                    updateResourceInfo,
                    resourceUrlInfo,
                    getResourceResult,
                    putResult
                )
            }
            is FailedPutResourceResult -> {
                deploymentListener.deploymentFailure(rawJsonResourceInfo, putResult)
                false
            }
            is SuccessPutResourceResult -> {
                deploymentListener.deploymentSuccess(rawJsonResourceInfo, putResult)
                true
            }
        }
    }

    fun attemptConflictResolution(
        rawJsonResourceInfo: RawJsonResourceInfo,
        updateJsonResourceInfo: RawJsonResourceInfo,
        resourceUrlInfo: ResourceUrlInfo,
        getResourceResult: ExistsGetResourceResult,
        putResourceResult: ConflictPutResourceResult
    ): Boolean {
        val currentJsonPathContext = JsonPath.parse(getResourceResult.json.toString())
        val newJsonPathContext = JsonPath.parse(updateJsonResourceInfo.json.toString())

        val statusObject = JSONObject(putResourceResult.response.body)
        val statusReason = statusObject.getString("reason")

        var newJsonUpdated = false
        var recreateNeeded = false

        if (statusReason.toLowerCase() == "invalid") {

            val detailsObject = statusObject.getJSONObject("details")
            val causesArray = detailsObject.getJSONArray("causes")

            var causesArrayIndex = 0
            while (!recreateNeeded && causesArrayIndex < causesArray.length()) {
                val causeObject = causesArray.getJSONObject(causesArrayIndex)

                val causeReason = causeObject.getString("reason")
                val causeMessage = causeObject.getString("message")
                val causeField = causeObject.getString("field")

                if (causeReason == "FieldValueInvalid" && causeMessage.endsWith("field is immutable")) {
                    // copy from original if the new object does not specify it
                    val fieldJsonPath = "$.$causeField"
                    val currentFieldValue = currentJsonPathContext.read<Any>(fieldJsonPath)

                    val newFieldValue = try {
                        newJsonPathContext.read<Any>(fieldJsonPath)
                    } catch (e: PathNotFoundException) {
                        null
                    }

                    if (newFieldValue == null) {
                        val lastDotIndex = fieldJsonPath.lastIndexOf('.')
                        val pathPart = fieldJsonPath.substring(0, lastDotIndex)
                        val keyPart = fieldJsonPath.substring(lastDotIndex + 1)

                        newJsonPathContext.put(pathPart, keyPart, currentFieldValue)

                        newJsonUpdated = true
                    } else {
                        // new resource definition -> requires update of immutable field
                        recreateNeeded = true
                    }
                }

                causesArrayIndex++
            }

        }

        return when {
            newJsonUpdated -> {
                val retryResourceInfo = RawJsonResourceInfo(rawJsonResourceInfo, JSONObject(newJsonPathContext.jsonString()))

                when (val retryPutResult = apiServerIntegration.putResource(retryResourceInfo, resourceUrlInfo)) {
                    is ConflictPutResourceResult -> {
                        deploymentListener.deploymentFailure(retryResourceInfo, putResourceResult)
                        false
                    }
                    is FailedPutResourceResult -> {
                        deploymentListener.deploymentFailure(retryResourceInfo, putResourceResult)
                        false
                    }
                    is SuccessPutResourceResult -> {
                        deploymentListener.deploymentSuccess(retryResourceInfo, retryPutResult)
                        true
                    }
                }
            }
            recreateNeeded -> {
                if (flags.recreateAllowed) {
                    val deleteResult = apiServerIntegration.deleteResource(resourceUrlInfo)

                    when (deleteResult) {
                        is FailedDeleteResourceResult -> {
                            deploymentListener.deploymentFailure(rawJsonResourceInfo, putResourceResult)
                            false
                        }
                        is SuccessDeleteResourceResult -> {
                            this.createResource(rawJsonResourceInfo, resourceUrlInfo)
                        }
                    }
                } else {
                    deploymentListener.deploymentFailure(rawJsonResourceInfo, putResourceResult)
                    false
                }
            }
            else -> {
                deploymentListener.deploymentFailure(rawJsonResourceInfo, putResourceResult)
                false
            }
        }
    }
}