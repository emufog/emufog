import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.10")
    }
}

val emufogMainClass = "emufog.EmufogKt"
val emufogVersion = "1.0"

plugins {
    kotlin("jvm") version "1.4.10"
    application
    jacoco
    id("org.jetbrains.dokka") version "0.10.0"
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("com.github.ajalt:clikt:2.4.0")
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.10.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.4.10")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.10")

    runtimeOnly("org.slf4j:slf4j-simple:1.7.30")

    testImplementation("org.junit.jupiter:junit-jupiter:5.6.0")
    testImplementation("io.mockk:mockk:1.9.3")
}

application {
    mainClassName = emufogMainClass
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs = listOf("-Xjsr305=strict")
    }
}

tasks {
    check {
        dependsOn(jacocoTestReport)
    }

    test {
        useJUnitPlatform()
    }

    jar {
        manifest {
            attributes["Implementation-Title"] = "EmuFog"
            attributes["Implementation-Version"] = emufogVersion
            attributes["Main-Class"] = emufogMainClass
        }
    }

    jacocoTestReport {
        reports {
            xml.isEnabled = true
            xml.destination = file("${buildDir}/reports/jacoco/report.xml")
            html.isEnabled = false
            csv.isEnabled = false
        }
    }

    dokka {
        outputFormat = "html"
        outputDirectory = "${buildDir}/dokka"
    }
}
