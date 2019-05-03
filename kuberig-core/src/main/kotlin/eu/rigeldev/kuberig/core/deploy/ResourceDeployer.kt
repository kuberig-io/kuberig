package eu.rigeldev.kuberig.core.deploy

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.core.execution.ResourceGeneratorMethodResult
import com.mashape.unirest.http.Unirest

class ResourceDeployer(val environment: KubeRigEnvironment) {

    fun deploy(methodResult: ResourceGeneratorMethodResult): ResourceGeneratorMethodResult {

        /*
            I0501 22:15:15.134272    2696 round_trippers.go:419] curl -k -v -XGET  -H "Accept: application/json" -H "User-Agent: kubectl/v1.14.1 (linux/amd64) kubernetes/b739410" 'http://127.0.0.1:8080/api/v1/namespaces/default/services/ktrack-mysql'
            I0501 22:15:15.135620    2696 round_trippers.go:438] GET http://127.0.0.1:8080/api/v1/namespaces/default/services/ktrack-mysql 404 Not Found in 1 milliseconds
            I0501 22:15:15.135646    2696 round_trippers.go:444] Response Headers:
            I0501 22:15:15.135652    2696 round_trippers.go:447]     Content-Type: application/json
            I0501 22:15:15.135665    2696 round_trippers.go:447]     Date: Wed, 01 May 2019 20:15:15 GMT
            I0501 22:15:15.135673    2696 round_trippers.go:447]     Content-Length: 200
            I0501 22:15:15.135723    2696 request.go:942] Response Body: {"kind":"Status","apiVersion":"v1","metadata":{},"status":"Failure","message":"services \"ktrack-mysql\" not found","reason":"NotFound","details":{"name":"ktrack-mysql","kind":"services"},"code":404}
            I0501 22:15:15.136018    2696 request.go:942] Request Body: {"apiVersion":"v1","kind":"Service","metadata":{"annotations":{"kubectl.kubernetes.io/last-applied-configuration":"{\"apiVersion\":\"v1\",\"kind\":\"Service\",\"metadata\":{\"annotations\":{},\"name\":\"ktrack-mysql\",\"namespace\":\"default\"},\"spec\":{\"ports\":[{\"port\":3306}],\"selector\":{\"app\":\"ktrack-mysql\"}}}\n"},"name":"ktrack-mysql","namespace":"default"},"spec":{"ports":[{"port":3306}],"selector":{"app":"ktrack-mysql"}}}
            I0501 22:15:15.136066    2696 round_trippers.go:419] curl -k -v -XPOST  -H "User-Agent: kubectl/v1.14.1 (linux/amd64) kubernetes/b739410" -H "Accept: application/json" -H "Content-Type: application/json" 'http://127.0.0.1:8080/api/v1/namespaces/default/services'
            I0501 22:15:15.144142    2696 round_trippers.go:438] POST http://127.0.0.1:8080/api/v1/namespaces/default/services 201 Created in 8 milliseconds
            I0501 22:15:15.144205    2696 round_trippers.go:444] Response Headers:
            I0501 22:15:15.144222    2696 round_trippers.go:447]     Content-Type: application/json
            I0501 22:15:15.144239    2696 round_trippers.go:447]     Date: Wed, 01 May 2019 20:15:15 GMT
            I0501 22:15:15.144255    2696 round_trippers.go:447]     Content-Length: 756
            I0501 22:15:15.144342    2696 request.go:942] Response Body: {"kind":"Service","apiVersion":"v1","metadata":{"name":"ktrack-mysql","namespace":"default","selfLink":"/api/v1/namespaces/default/services/ktrack-mysql","uid":"d4e29cb6-6c4d-11e9-8de1-38d54779e1ed","resourceVersion":"915871","creationTimestamp":"2019-05-01T20:15:15Z","annotations":{"kubectl.kubernetes.io/last-applied-configuration":"{\"apiVersion\":\"v1\",\"kind\":\"Service\",\"metadata\":{\"annotations\":{},\"name\":\"ktrack-mysql\",\"namespace\":\"default\"},\"spec\":{\"ports\":[{\"port\":3306}],\"selector\":{\"app\":\"ktrack-mysql\"}}}\n"}},"spec":{"ports":[{"protocol":"TCP","port":3306,"targetPort":3306}],"selector":{"app":"ktrack-mysql"},"clusterIP":"10.152.183.126","type":"ClusterIP","sessionAffinity":"None"},"status":{"loadBalancer":{}}}
            service/ktrack-mysql created
         */

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

        val json = objectMapper.valueToTree<JsonNode>(methodResult.resource)

        val apiVersion = json.get("apiVersion").textValue().toLowerCase()
        val kind = json.get("kind").textValue().toLowerCase() + "s"
        val resourceName = json.get("metadata").get("name").textValue()

        val namespace = if (json.get("metadata").hasNonNull("namespace")) {
            json.get("metadata").get("namespace").textValue()
        } else {
            "default"
        }

        val targetUrl = "${environment.apiServer}/api/$apiVersion/namespaces/$namespace/$kind"

        println("Deploying $apiVersion - $kind to $targetUrl")

        val getResult = Unirest.get("$targetUrl/$resourceName")
            .asJson()

        if (getResult.status == 404) {
            val postResponse =
                Unirest.post(targetUrl)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .asString()

            if (postResponse.status != 200) {
                println(postResponse.status)
                println(postResponse.statusText)
                println(postResponse.body)
                System.out.println("Failed to create $kind from resource generator method ${methodResult.method.generatorType} - ${methodResult.method.methodName}")
            }
        } else {
            val putResponse =
                    Unirest.put(targetUrl +"/$resourceName")
                        .header("Content-Type", "application/json")
                        .body(json)
                        .asString()

            if (putResponse.status != 200) {
                println(putResponse.status)
                println(putResponse.statusText)
                println(putResponse.body)
                System.out.println("Failed to update $kind from resource generator method ${methodResult.method.generatorType} - ${methodResult.method.methodName}")
            }
        }




        return methodResult
    }

}