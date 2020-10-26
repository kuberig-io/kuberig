package io.kuberig.core.detection

interface JarFilePackageListener {

    fun receivePackage(packageName: String)

}