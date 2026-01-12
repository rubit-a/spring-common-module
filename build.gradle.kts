import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    kotlin("jvm") version "2.2.21" apply false
    kotlin("plugin.spring") version "2.2.21" apply false
    id("org.springframework.boot") version "4.0.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

val githubRepository = providers.gradleProperty("githubRepository").orNull
    ?: System.getenv("GITHUB_REPOSITORY")
val githubOwner = providers.gradleProperty("githubOwner").orNull
    ?: System.getenv("GITHUB_OWNER")
val githubRepo = providers.gradleProperty("githubRepo").orNull
    ?: System.getenv("GITHUB_REPO")
val githubPackagesUrl = providers.gradleProperty("githubPackagesUrl").orNull
    ?: when {
        !githubRepository.isNullOrBlank() -> "https://maven.pkg.github.com/$githubRepository"
        !githubOwner.isNullOrBlank() && !githubRepo.isNullOrBlank() ->
            "https://maven.pkg.github.com/$githubOwner/$githubRepo"
        else -> null
    }
val githubUsername = providers.gradleProperty("gpr.user").orNull
    ?: System.getenv("GITHUB_ACTOR")
    ?: System.getenv("GITHUB_USERNAME")
val githubToken = providers.gradleProperty("gpr.key").orNull
    ?: System.getenv("GITHUB_TOKEN")
    ?: System.getenv("GITHUB_PACKAGES_TOKEN")
val githubPackagesEnabled = !githubPackagesUrl.isNullOrBlank() &&
    !githubUsername.isNullOrBlank() &&
    !githubToken.isNullOrBlank()
val autoPublish = (providers.gradleProperty("autoPublish").orNull ?: "true").toBoolean()
val refreshSnapshots = (providers.gradleProperty("refreshSnapshots").orNull ?: "true").toBoolean()
val localModuleSubstitutions = mapOf(
    "rubit:core-security" to ":core-security",
    "rubit:core-security-oauth2" to ":core-security-oauth2",
    "rubit:core-web" to ":core-web",
    "rubit:core-logging" to ":core-logging",
    "rubit:core-data" to ":core-data",
    "rubit:core-excel" to ":core-excel",
    "rubit:core-test" to ":core-test"
)

fun org.gradle.api.artifacts.dsl.RepositoryHandler.githubPackages() {
    if (!githubPackagesEnabled) {
        return
    }

    maven {
        url = uri(githubPackagesUrl!!)
        credentials {
            username = githubUsername
            password = githubToken
        }
    }
}

allprojects {
    group = "rubit"
    version = (rootProject.findProperty("coreCommonVersion") as String?)
        ?: "1.0.0"

    repositories {
        mavenLocal()
        mavenCentral()
        githubPackages()
    }

    configurations.configureEach {
        if (refreshSnapshots) {
            resolutionStrategy.cacheChangingModulesFor(0, "seconds")
            resolutionStrategy.cacheDynamicVersionsFor(0, "seconds")
        }

        resolutionStrategy.dependencySubstitution {
            localModuleSubstitutions.forEach { (moduleId, projectPath) ->
                if (rootProject.findProject(projectPath) != null) {
                    substitute(module(moduleId)).using(project(projectPath))
                }
            }
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
                    githubPackages()
                }
            }
        }

        if (autoPublish && githubPackagesEnabled) {
            tasks.matching { it.name == "build" }.configureEach {
                dependsOn("publish")
            }
        }
    }
}
