plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.jpa") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "rubit"
version = "0.0.1-SNAPSHOT"
description = "core-test"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val useLocalModules = (findProperty("useLocalModules") as String?)?.toBoolean() ?: false
val commonModuleVersion = project.version.toString()

dependencies {
    // Common Auth Module
    if (useLocalModules) {
        implementation(project(":core-data"))
        implementation(project(":core-excel"))
        implementation(project(":core-logging"))
        implementation(project(":core-security"))
        implementation(project(":core-web"))
    } else {
        implementation("rubit:core-data:$commonModuleVersion")
        implementation("rubit:core-excel:$commonModuleVersion")
        implementation("rubit:core-logging:$commonModuleVersion")
        implementation("rubit:core-security:$commonModuleVersion")
        implementation("rubit:core-web:$commonModuleVersion")
    }

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aspectj")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("com.h2database:h2")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
