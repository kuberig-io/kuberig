import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    val kotlinVersion by extra("1.3.30")

    repositories {
        jcenter()
    }
    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

subprojects {
    apply {
        plugin("maven-publish")
        plugin("java")
        plugin("idea")
        plugin("org.jetbrains.kotlin.jvm")
    }

    val subProject = this

    subProject.group = "eu.rigeldev.kuberig"
    subProject.version = project.version

    repositories {
        jcenter()
    }

    dependencies {
        val implementation by configurations
        val testImplementation by configurations
        val testRuntimeOnly by configurations

        implementation(kotlin("stdlib-jdk8"))

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.2")
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
    }
    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }

    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")

        val sourceSets: SourceSetContainer by subProject
        from(sourceSets["main"].allSource)
    }

    configure<PublishingExtension> {

        publications {
            register(subProject.name, MavenPublication::class) {
                from(components["java"])
                artifact(sourcesJar.get())
            }
        }

    }

    if (subProject.hasProperty("bintrayUser")) {
        apply {
            plugin("com.jfrog.bintray")
        }

        configure<BintrayExtension> {
            val bintrayApiKey: String by subProject
            val bintrayUser: String by subProject

            user = bintrayUser
            key = bintrayApiKey
            publish = true

            pkg(closureOf<BintrayExtension.PackageConfig> {
                repo = "rigeldev-oss-maven"
                name = subProject.name
                setLicenses("Apache-2.0")
                vcsUrl = "https://github.com/teyckmans/kuberig-dsl"
            })

            setPublications(subProject.name)
        }
    }
}
