package eu.rigeldev.kuberig.ssl

import java.security.cert.X509Certificate

data class SslCertificateChain(val chain: List<X509Certificate>) {
    fun isRealChain(): Boolean {
        return this.chain.size > 1
    }

    fun caCertificate(): X509Certificate {
        return this.chain[0]
    }

    fun subjectIssuers(): List<Pair<String?, String?>> {
        return chain.map {
            Pair(it.subjectDN.name, it.issuerDN.name)
        }
    }
}