plugins {
    id("eu.rigeldev.kuberig")
}

repositories {
    jcenter()
}

kuberig {
    kubernetes("v1.12.8")

    environments {
        create("local") {
            apiServer = "http://localhost:8080"
        }
        
        create("dev") {
            apiServer = "http://somewhere-else:8080"
        }
    }
}