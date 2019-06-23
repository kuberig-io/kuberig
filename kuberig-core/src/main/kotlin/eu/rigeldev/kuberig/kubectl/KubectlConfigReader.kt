package eu.rigeldev.kuberig.kubectl

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import eu.rigeldev.kuberig.core.generation.yaml.ByteArrayDeserializer
import eu.rigeldev.kuberig.core.generation.yaml.ByteArraySerializer
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.File
import java.io.IOException
import java.security.KeyStore
import java.security.Security
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Pointer to the go types kubectl is using to read the kubectl config file:
 *
 * https://github.com/kubernetes/kubernetes/blob/ee4776d7ecb0e186c377e273d72f1ae957c44170/staging/src/k8s.io/client-go/tools/clientcmd/api/types.go#L31-L53
 *
 */
class KubectlConfigReader {

    fun readKubectlConfig(): ContextResult {

        val yamlFactory = YAMLFactory()
        yamlFactory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)

        val objectMapper = ObjectMapper(yamlFactory)
        objectMapper.findAndRegisterModules()

        val byteArrayModule = SimpleModule()
        byteArrayModule.addSerializer(ByteArray::class.java, ByteArraySerializer())
        byteArrayModule.addDeserializer(ByteArray::class.java, ByteArrayDeserializer())

        objectMapper.registerModule(byteArrayModule)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)

        val configFileLocation = System.getenv("KUBECONFIG") ?: "${System.getProperty("user.home")}/.kube/config"

        val configFile = File(configFileLocation)

        val tree = objectMapper.readTree(configFile)

        val currentContextName = tree.get("current-context").textValue()

        println("Using current-context: $currentContextName")

        val contextsNode = tree.get("contexts") as ArrayNode
        val clustersNode = tree.get("clusters") as ArrayNode
        val usersNode = tree.get("users") as ArrayNode

        val contextDetail = contextDetail(contextsNode, currentContextName)

        if (contextDetail != null) {
            var clusterDetail = clusterDetail(clustersNode, contextDetail.clusterName)

            if (clusterDetail != null) {
                println("Using server: ${clusterDetail.server}")
                println("Using namespace: ${contextDetail.namespace}")
                println("Using ca-certificate: \n ${clusterDetail.certificateAuthorityData}")


            } else {
                return ErrorContextResult("Could not find cluster details for cluster with name ${contextDetail.clusterName} referenced from current context $currentContextName")
            }

            var userDetail = userDetail(usersNode, contextDetail.userName)

            if (userDetail != null) {
                if (userDetail is ClientCertificateUserDetail) {

                    println("Using client-certificate: \n ${userDetail.clientCertificateData}")
                    println("Using client-key: \n ${userDetail.clientKeyData}")

                } else {
                    return ErrorContextResult("User detail type not supported")
                }
            } else {
                return ErrorContextResult("Could not find user details for user with name ${contextDetail.userName} referenced from current context $currentContextName")
            }

            val authDetails = this.buildAuthDetails(clusterDetail, userDetail)

            return if (authDetails == null) {
                ErrorContextResult("Failed to create authentication details")
            } else {
                OkContextResult(
                        clusterDetail,
                        userDetail,
                        authDetails,
                        contextDetail.namespace
                )
            }

        } else {
            return ErrorContextResult("Could not find context details for context with name $currentContextName")
        }
    }

    private fun buildAuthDetails(clusterDetail: ClusterDetail, userDetail: UserDetail): AuthDetails? {
        if (userDetail is ClientCertificateUserDetail) {
            Security.addProvider(BouncyCastleProvider())

            val pp = PEMParser(userDetail.clientKeyData.reader())
            val pemKeyPair = pp.readObject() as PEMKeyPair
            val kp = JcaPEMKeyConverter().getKeyPair(pemKeyPair)
            pp.close()

            val certificateFactory = CertificateFactory.getInstance("X.509")
            val cert = certificateFactory.generateCertificate(userDetail.clientCertificateData.byteInputStream()) as X509Certificate

            val caCert = certificateFactory.generateCertificate(clusterDetail.certificateAuthorityData.byteInputStream()) as X509Certificate

            val keyStore = KeyStore.getInstance("JKS")
            keyStore.load(null)
            keyStore.setKeyEntry("user", kp.private, "changeit".toCharArray(), arrayOf(cert, caCert))

            return ClientCertAuthDetails(keyStore)
        } else {
            return null
        }
    }

    private fun contextDetail(contextsNode: ArrayNode, currentContextName: String): ContextDetail? {
        var contextDetail: ContextDetail? = null

        var currentIndex = 0
        while (contextDetail == null && currentIndex < contextsNode.size()) {
            val currentNode = contextsNode.get(currentIndex)

            if (currentNode.get("name").textValue() == currentContextName) {
                val contextNode = currentNode.get("context")

                val namespace = if (contextNode.has("namespace")) {
                    contextNode.get("namespace").textValue()
                } else {
                    "default"
                }

                contextDetail = ContextDetail(
                        contextNode.get("cluster").textValue(),
                        contextNode.get("user").textValue(),
                        namespace
                )
            }

            currentIndex++
        }
        return contextDetail
    }

    private fun clusterDetail(clustersNode: ArrayNode, clusterName: String): ClusterDetail? {
        var clusterDetail: ClusterDetail? = null

        var currentIndex = 0
        while (clusterDetail == null && currentIndex < clustersNode.size()) {
            val currentNode = clustersNode.get(currentIndex)

            if (currentNode.get("name").textValue() == clusterName) {
                val clusterNode = currentNode.get("cluster")

                val certificateAuthorityData = if (clusterNode.has("certificate-authority")) {
                    File(clusterNode.get("certificate-authority").textValue()).readText()
                } else {
                    String(Base64.getDecoder().decode(clusterNode.get("certificate-authority-data").textValue()))
                }

                clusterDetail = ClusterDetail(
                        certificateAuthorityData,
                        clusterNode.get("server").textValue()
                )
            }

            currentIndex++
        }

        return clusterDetail
    }

    private fun userDetail(usersNode: ArrayNode, userName: String): UserDetail? {
        var userDetail: UserDetail? = null

        var currentIndex = 0
        while (userDetail == null && currentIndex < usersNode.size()) {
            val currentNode = usersNode.get(currentIndex)

            if (currentNode.get("name").textValue() == userName) {
                val userNode = currentNode.get("user") as ObjectNode

                userDetail = toUserDetail(userNode)
            }

            currentIndex++
        }

        return userDetail
    }

    private fun toUserDetail(userNode: ObjectNode): UserDetail? {
        if (userNode.has("exec")) {
            val execNode = userNode.get("exec")

            val command = execNode.get("command").textValue()
            val argsNode = execNode.get("args") as ArrayNode

            var fullCommand = command

            var currentIndex = 0
            while (currentIndex < argsNode.size()) {
                val currentNode = argsNode.get(currentIndex)

                fullCommand = "$fullCommand ${currentNode.textValue()}"

                currentIndex++
            }

            // todo deal with 'env' node

            val json = ObjectMapper()
            json.findAndRegisterModules()

            val execCredentialsKind = json.readTree(fullCommand.runCommand())

            val statusNode = execCredentialsKind.get("status")

            return ClientCertificateUserDetail(
                    statusNode.get("clientCertificateData").textValue(),
                    statusNode.get("clientKeyData").textValue()
            )
        }
        else if (userNode.has("client-certificate") && userNode.has("client-key")) {
            return ClientCertificateUserDetail(
                    File(userNode.get("client-certificate").textValue()).readText(),
                    File(userNode.get("client-key").textValue()).readText()
            )
        }
        else if (userNode.has("auth-provider")) {
            val authProviderNode = userNode.get("auth-provider")

            val authProviderName = authProviderNode.get("name").textValue()

            println("User configuration via auth-provider with name $authProviderName is not yet support")

            return null
        }
        else {
            println("User configuration is not supported yet \n $userNode")
            return null
        }
    }

}

data class ContextDetail(val clusterName: String, val userName: String, val namespace: String = "")
data class ClusterDetail(val certificateAuthorityData: String, val server: String)
sealed class UserDetail
data class ClientCertificateUserDetail(val clientCertificateData: String, val clientKeyData: String) : UserDetail()

sealed class AuthDetails
data class ClientCertAuthDetails(val keyStore: KeyStore) : AuthDetails()

sealed class ContextResult

data class OkContextResult(
        val clusterDetail: ClusterDetail,
        val userDetail: UserDetail,
        val authDetails: AuthDetails,
        val namespace: String
) : ContextResult()

class ErrorContextResult(val error: String) : ContextResult()

fun String.runCommand(workingDir: File = File("."),
                      timeoutAmount: Long = 60,
                      timeoutUnit: TimeUnit = TimeUnit.SECONDS): String? {
    return try {
        ProcessBuilder(*this.split("\\s".toRegex()).toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)

                .start().apply {
                    waitFor(timeoutAmount, timeoutUnit)
                }.inputStream.bufferedReader().readText()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}