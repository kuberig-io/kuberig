plugins {
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.11.0"
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
            id = "io.kuberig.kuberig"
            displayName = "Kuberig plugin"
            description = "This plugin is used to deploy to Kubernetes or Openshift using a Kotlin DSL to define the resources that need to be deploy."
            implementationClass = "io.kuberig.gradle.KubeRigPlugin"
        }
    }
}

pluginBundle {
    website = project.properties["websiteUrl"]!! as String
    vcsUrl = project.properties["vcsUrl"]!! as String
    tags = listOf("kubernetes", "kotlin", "dsl", "openshift")
}

(tasks.getByName("processResources") as ProcessResources).apply {
    filesMatching("io.kuberig.kuberig.properties") {
        expand(
            Pair("kuberigVersion", project.version.toString()),
            Pair("kotlinVersion", project.properties["kotlinVersion"]),
            Pair("kuberigDslVersion", project.properties["kuberigDslVersion"])
        )
    }
}