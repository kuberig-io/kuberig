dependencies {
    implementation(project(":kuberig-annotations"))
    implementation(project(":kuberig-dsl-support"))

    val kuberigDslVersion : String by project
    
    implementation("eu.rigeldev.kuberig:kuberig-dsl-base:${kuberigDslVersion}")

    val jacksonVersion = "2.9.8"
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")

    implementation("org.ow2.asm:asm:7.1")

    implementation("com.konghq:unirest-java:2.3.14")
    implementation("com.google.crypto.tink:tink:1.2.2")

    implementation("org.bouncycastle:bcprov-jdk15on:1.62")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.62")

    implementation("com.jayway.jsonpath:json-path:2.4.0")

    implementation("org.javassist:javassist:3.27.0-GA")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")

    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
}