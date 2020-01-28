import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.61")
    }
}

val emufogMainClass = "emufog.EmufogKt"
val emufogVersion = "1.0"

plugins {
    kotlin("jvm") version "1.3.61"
    jacoco
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("info.picocli:picocli:4.0.0")
    implementation("org.slf4j:slf4j-api:1.7.26")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.9.9")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.9")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.61")

    runtime("ch.qos.logback:logback-classic:1.2.3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("io.mockk:mockk:1.9.3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
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
        from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    jacocoTestReport {
        reports {
            xml.isEnabled = false
            csv.isEnabled = false
            html.destination = file("${buildDir}/jacocoHtml")
        }
    }
}