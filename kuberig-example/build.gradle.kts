import java.time.Duration

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
            // tested with microk8s
            apiServer = "http://localhost:8080"
        }
        
        create("dev") {
            apiServer = "https://58788ad0-0a17-4f15-a76f-c0406bcd7e40.k8s.ondigitalocean.com"
        }
    }

    deployControl {
        tickRange = IntRange(1, 2)
        tickDuration = Duration.ofSeconds(10)
        tickGateKeeper = "eu.rigeldev.kuberig.core.deploy.control.DefaultTickGateKeeper"
    }
}