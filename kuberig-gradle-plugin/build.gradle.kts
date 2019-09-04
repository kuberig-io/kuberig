plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.10.1"
}

dependencies {
    val kotlinVersion = project.properties["kotlinVersion"]
    
    implementation(project(":kuberig-core"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    implementation("com.google.crypto.tink:tink:1.2.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.5.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.5.1")
}

gradlePlugin {
    plugins {
        create("kuberig-gradle-plugin") {
            id = "eu.rigeldev.kuberig"
            displayName = "Kuberig plugin"
            description = "This plugin is used to deploy to Kubernetes or Openshift using a Kotlin DSL to define the resources that need to be deploy."
            implementationClass = "eu.rigeldev.kuberig.gradle.KubeRigPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/teyckmans/kuberig"
    vcsUrl = "https://github.com/teyckmans/kuberig"
    tags = listOf("kubernetes", "kotlin", "dsl", "openshift")
}

tasks.withType<ProcessResources> {
    filesMatching("kuberig.properties") {
        expand(
            Pair("kuberigVersion", project.version.toString()),
            Pair("kotlinVersion", project.properties["kotlinVersion"]),
            Pair("kuberigDslVersion", project.properties["kuberigDslVersion"])
        )
    }
}