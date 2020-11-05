dependencies {
    val kuberigDslVersion : String by project
    
    implementation("io.kuberig:kuberig-dsl-base:${kuberigDslVersion}")

    implementation("org.slf4j:slf4j-api:1.7.26")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
}