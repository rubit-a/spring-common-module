plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.jpa") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "rubit"
version = (rootProject.findProperty("coreTestVersion") as String?)
    ?: "1.0.0"
description = "core-test"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

val coreSecurityVersion = (rootProject.findProperty("coreSecurityVersion") as String?)
    ?: rootProject.version.toString()
val coreDataVersion = (rootProject.findProperty("coreDataVersion") as String?)
    ?: rootProject.version.toString()
val coreExcelVersion = (rootProject.findProperty("coreExcelVersion") as String?)
    ?: rootProject.version.toString()
val coreLoggingVersion = (rootProject.findProperty("coreLoggingVersion") as String?)
    ?: rootProject.version.toString()
val coreWebVersion = (rootProject.findProperty("coreWebVersion") as String?)
    ?: rootProject.version.toString()

dependencies {
    implementation("rubit:core-data:$coreDataVersion")
    implementation("rubit:core-excel:$coreExcelVersion")
    implementation("rubit:core-logging:$coreLoggingVersion")
    implementation("rubit:core-security:$coreSecurityVersion")
    implementation("rubit:core-web:$coreWebVersion")

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
