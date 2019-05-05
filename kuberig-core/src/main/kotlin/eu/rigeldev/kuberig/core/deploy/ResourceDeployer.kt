package eu.rigeldev.kuberig.core.deploy

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.mashape.unirest.http.Unirest
import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorMethodResult
import eu.rigeldev.kuberig.core.execution.SuccessResult

class ResourceDeployer(private val environment: KubeRigEnvironment) {

    fun deploy(methodResult: ResourceGeneratorMethodResult): ResourceGeneratorMethodResult {

        val objectMapper = ObjectMapper()
        objectMapper.findAndRegisterModules()

        Unirest.setObjectMapper(object: com.mashape.unirest.http.ObjectMapper {
            override fun writeValue(value: Any?): String {
                return objectMapper.writeValueAsString(value)
            }

            override fun <T : Any?> readValue(value: String?, valueType: Class<T>?): T {
                return objectMapper.readValue(value, valueType)
            }
        })

        if (methodResult is SuccessResult) {
            val json = objectMapper.valueToTree<JsonNode>(methodResult.resource)

            val apiVersion = json.get("apiVersion").textValue().toLowerCase()
            val kind = json.get("kind").textValue()
            val resourceName = json.get("metadata").get("name").textValue()

            val apiResourceList = Unirest.get("${environment.apiServer}/api/$apiVersion")
                .asObject(APIResourceList::class.java)

            val apiResource = apiResourceList.body.resources.first{ it.kind == kind }

            val namespace = if (json.get("metadata").hasNonNull("namespace")) {
                json.get("metadata").get("namespace").textValue()
            } else {
                "default"
            }

            val targetUrl = "${environment.apiServer}/api/$apiVersion/namespaces/$namespace/${apiResource.name}"

            val getResult = Unirest.get("$targetUrl/$resourceName")
                .asJson()

            if (getResult.status == 404) {
                val postResponse =
                    Unirest.post(targetUrl)
                        .header("Content-Type", "application/json")
                        .body(json)
                        .asString()

                if (postResponse.status != 201) {
                    println(postResponse.status)
                    println(postResponse.statusText)
                    println(postResponse.body)
                    System.out.println("Failed to create $kind from resource generator method ${methodResult.method.generatorType} - ${methodResult.method.methodName}")
                } else {
                    println("created $kind - $resourceName in $namespace namespace")
                }
            } else {
                val putResponse =
                    Unirest.put("$targetUrl/$resourceName")
                        .header("Content-Type", "application/json")
                        .body(json)
                        .asString()

                if (putResponse.status != 200) {
                    println(putResponse.status)
                    println(putResponse.statusText)
                    println(putResponse.body)
                    System.out.println("Failed to update $kind from resource generator method ${methodResult.method.generatorType} - ${methodResult.method.methodName}")
                } else {
                    println("updated $kind - $resourceName in $namespace namespace")
                }
            }
        }

        return methodResult
    }
}