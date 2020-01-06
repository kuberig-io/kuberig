import com.jfrog.bintray.gradle.BintrayExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    val kotlinVersion by extra("1.3.50")

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

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.1")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.1")
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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

    if (System.getenv("BINTRAY_API_KEY") != null){
        apply {
            plugin("com.jfrog.bintray")
        }

        configure<BintrayExtension> {
            val bintrayApiKey = System.getenv("BINTRAY_API_KEY")
            val bintrayUser = System.getenv("BINTRAY_USER")

            user = bintrayUser
            key = bintrayApiKey
            publish = true

            pkg(closureOf<BintrayExtension.PackageConfig> {
                repo = "rigeldev-oss-maven"
                name = subProject.name
                setLicenses("Apache-2.0")
                vcsUrl = "https://github.com/teyckmans/kuberig"
            })

            setPublications(subProject.name)
        }
    }
}
