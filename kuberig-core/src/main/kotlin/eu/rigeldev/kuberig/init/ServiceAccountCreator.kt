package eu.rigeldev.kuberig.init

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.mashape.unirest.http.Unirest
import eu.rigeldev.kuberig.encryption.EncryptionSupport
import eu.rigeldev.kuberig.kubectl.AccessTokenAuthDetail
import eu.rigeldev.kuberig.kubectl.ClientCertAuthDetails
import eu.rigeldev.kuberig.kubectl.OkContextResult
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.conn.BasicHttpClientConnectionManager
import org.apache.http.ssl.SSLContexts
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*

class ServiceAccountCreator {

    fun createDefaultServiceAccount(environmentName: String, contextResult: OkContextResult, environmentEncryptionSupport: EncryptionSupport, environmentDirectory: File) {
        val certificateFactory = CertificateFactory.getInstance("X.509")
        val caCert = certificateFactory.generateCertificate(contextResult.clusterDetail.certificateAuthorityData.byteInputStream()) as X509Certificate

        val trustStore = KeyStore.getInstance("JKS")
        trustStore.load(null)
        trustStore.setCertificateEntry("server", caCert)

        var keyStore: KeyStore? = null

        if (contextResult.authDetails is ClientCertAuthDetails) {
            keyStore = contextResult.authDetails.keyStore
        } else if (contextResult.authDetails is AccessTokenAuthDetail) {
            Unirest.clearDefaultHeaders()
            Unirest.setDefaultHeader("Authorization", "Bearer ${contextResult.authDetails.accessToken}")
        } else {
            println("Auth details not supported")
        }

        val objectMapper = ObjectMapper()
        objectMapper.findAndRegisterModules()
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT)

        Unirest.setObjectMapper(object : com.mashape.unirest.http.ObjectMapper {
            override fun writeValue(value: Any?): String {
                return objectMapper.writeValueAsString(value)
            }

            override fun <T : Any?> readValue(value: String?, valueType: Class<T>?): T {
                return objectMapper.readValue(value, valueType)
            }
        })

        val sslContextBuilder = SSLContexts.custom()
                .loadTrustMaterial(trustStore, null)

        if (keyStore != null) {
            sslContextBuilder.loadKeyMaterial(keyStore, "changeit".toCharArray())
        }

        val sslcontext = sslContextBuilder
                .build()

        val sslsf = SSLConnectionSocketFactory(sslcontext)

        val clientBuilder = HttpClientBuilder.create()

        clientBuilder.setSSLSocketFactory(sslsf)
        val registry = RegistryBuilder.create<ConnectionSocketFactory>()
                .register("https", sslsf)
                .register("http", PlainConnectionSocketFactory())
                .build()
        val ccm = BasicHttpClientConnectionManager(registry)
        clientBuilder.setConnectionManager(ccm)

        val httpclient = clientBuilder.build()
        Unirest.setHttpClient(httpclient)

        val apiServer = contextResult.clusterDetail.server

        val serviceAccountName = "kuberig"
        val namespace = contextResult.namespace

        // create service account
        // POST - https://64260954-bb12-4801-b518-70e331c1c6bf.k8s.ondigitalocean.com/api/v1/namespaces/default/serviceaccounts
        // {"apiVersion":"v1","kind":"ServiceAccount","metadata":{"creationTimestamp":null,"name":"kuberig-deployer"}}
        val sa = JSONObject()
                .put("apiVersion", "v1")
                .put("kind", "ServiceAccount")
                .put("metadata", JSONObject()
                        .put("name", serviceAccountName)
                        .put("namespace", namespace)
                )
        val saPost = Unirest.post("$apiServer/api/v1/namespaces/$namespace/serviceaccounts")
                .header("Content-Type", "application/json")
                .body(sa)
                .asJson()

        if (saPost.status == 201) {
            println("Created $serviceAccountName service account in namespace $namespace.")

            // create role binding (edit)
            // POST POST https://64260954-bb12-4801-b518-70e331c1c6bf.k8s.ondigitalocean.com/apis/rbac.authorization.k8s.io/v1/namespaces/default/rolebindings
            // {"apiVersion":"rbac.authorization.k8s.io/v1","kind":"RoleBinding","metadata":{"creationTimestamp":null,"name":"kuberig-deployer-edit"},"roleRef":{"apiGroup":"rbac.authorization.k8s.io","kind":"ClusterRole","name":"edit"},"subjects":[{"kind":"ServiceAccount","name":"kuberig-deployer","namespace":"default"}

            val rb = JSONObject()
                    .put("apiVersion", "rbac.authorization.k8s.io/v1")
                    .put("kind", "RoleBinding")
                    .put("metadata",
                            JSONObject()
                                    .put("name", "$serviceAccountName-edit")
                                    .put("namespace", namespace)
                    )
                    .put("roleRef",
                            JSONObject()
                                    .put("apiGroup", "rbac.authorization.k8s.io")
                                    .put("kind", "ClusterRole")
                                    .put("name", "edit")
                    )
                    .put("subjects",
                            JSONArray(
                                    arrayOf(
                                            JSONObject()
                                                    .put("kind", "ServiceAccount")
                                                    .put("name", serviceAccountName)
                                                    .put("namespace", namespace)
                                    )
                            )

                    )

            val rbPost = Unirest.post("$apiServer/apis/rbac.authorization.k8s.io/v1/namespaces/$namespace/rolebindings")
                    .header("Content-Type", "application/json")
                    .body(rb)
                    .asJson()

            if (rbPost.status == 201) {
                println("Created edit role binding for $serviceAccountName service account in namespace $namespace")

                // describe sa
                // GET https://64260954-bb12-4801-b518-70e331c1c6bf.k8s.ondigitalocean.com/api/v1/namespaces/default/serviceaccounts/kuberig-deployer
                // {"kind":"ServiceAccount","apiVersion":"v1","metadata":{"name":"kuberig-deployer","namespace":"default","selfLink":"/api/v1/namespaces/default/serviceaccounts/kuberig-deployer","uid":"c1d6e5eb-938e-11e9-8593-3a05544fda29","resourceVersion":"128506","creationTimestamp":"2019-06-20T19:08:15Z"},"secrets":[{"name":"kuberig-deployer-token-m4kdm"}]}
                val saDescGet = Unirest.get("$apiServer/api/v1/namespaces/$namespace/serviceaccounts/$serviceAccountName")
                        .asJson()

                val saDesc = saDescGet.body.getObject()
                val secretName = saDesc.getJSONArray("secrets").getJSONObject(0).getString("name")

                // describe secret
                // GET https://64260954-bb12-4801-b518-70e331c1c6bf.k8s.ondigitalocean.com/api/v1/namespaces/default/secrets/kuberig-deployer-token-m4kdm
                val secretGet = Unirest.get("$apiServer/api/v1/namespaces/$namespace/secrets/$secretName")
                        .asJson()

                val token = secretGet.body.getObject().getJSONObject("data").getString("token")

                val defaultAccessTokenFile = File(environmentDirectory, ".plain.$environmentName.access-token")

                defaultAccessTokenFile.writeText(
                        String(Base64.getDecoder().decode(token))
                )

                environmentEncryptionSupport.encryptFile(defaultAccessTokenFile)

                defaultAccessTokenFile.delete()

            } else {
                println("Failed to create edit role binding")
            }
        } else {
            println("Failed to create service account")
        }


        // describe sa
        // GET https://64260954-bb12-4801-b518-70e331c1c6bf.k8s.ondigitalocean.com/api/v1/namespaces/default/serviceaccounts/kuberig-deployer
        // {"kind":"ServiceAccount","apiVersion":"v1","metadata":{"name":"kuberig-deployer","namespace":"default","selfLink":"/api/v1/namespaces/default/serviceaccounts/kuberig-deployer","uid":"c1d6e5eb-938e-11e9-8593-3a05544fda29","resourceVersion":"128506","creationTimestamp":"2019-06-20T19:08:15Z"},"secrets":[{"name":"kuberig-deployer-token-m4kdm"}]}

        // describe secret
        // GET https://64260954-bb12-4801-b518-70e331c1c6bf.k8s.ondigitalocean.com/api/v1/namespaces/default/secrets/kuberig-deployer-token-m4kdm
        //
    }

}