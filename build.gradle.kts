import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    kotlin("jvm") version "2.2.21" apply false
    kotlin("plugin.spring") version "2.2.21" apply false
    id("org.springframework.boot") version "4.0.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

val nexusBaseUrl = providers.gradleProperty("nexusBaseUrl").orNull ?: "http://localhost:8081"
val nexusPublicUrl = providers.gradleProperty("nexusPublicUrl").orNull
    ?: "$nexusBaseUrl/repository/maven-public/"
val nexusReleasesUrl = providers.gradleProperty("nexusReleasesUrl").orNull
    ?: "$nexusBaseUrl/repository/maven-releases/"
val nexusSnapshotsUrl = providers.gradleProperty("nexusSnapshotsUrl").orNull
    ?: "$nexusBaseUrl/repository/maven-snapshots/"
val nexusUsername = providers.gradleProperty("nexusUsername").orNull ?: System.getenv("NEXUS_USERNAME")
val nexusPassword = providers.gradleProperty("nexusPassword").orNull ?: System.getenv("NEXUS_PASSWORD")

allprojects {
    group = "rubit"
    version = "0.0.1-SNAPSHOT"

    repositories {
        maven {
            url = uri(nexusPublicUrl)
            isAllowInsecureProtocol = nexusPublicUrl.startsWith("http://")
        }
    }
}

subprojects {
    if (name != "core-test") {
        apply(plugin = "maven-publish")

        plugins.withId("org.jetbrains.kotlin.jvm") {
            extensions.configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("mavenJava") {
                        from(components["java"])
                    }
                }
                repositories {
                    val publishUrl = if (project.version.toString().endsWith("SNAPSHOT")) {
                        nexusSnapshotsUrl
                    } else {
                        nexusReleasesUrl
                    }
                    maven {
                        url = uri(publishUrl)
                        isAllowInsecureProtocol = publishUrl.startsWith("http://")
                        if (!nexusUsername.isNullOrBlank()) {
                            credentials {
                                username = nexusUsername
                                password = nexusPassword
                            }
                        }
                    }
                }
            }
        }
    }
}
