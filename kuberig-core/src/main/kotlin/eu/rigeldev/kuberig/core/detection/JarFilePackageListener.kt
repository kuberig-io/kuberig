package eu.rigeldev.kuberig.core.detection

interface JarFilePackageListener {

    fun receivePackage(packageName: String)

}