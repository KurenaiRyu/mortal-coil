import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
//    id("org.graalvm.buildtools.native") version "0.9.20"
}

group = "moe.kurenai.coil"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

object Versions {
    const val log4j = "2.20.0"
    const val ktor = "2.3.0"
}
dependencies {

    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.9.10"))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("io.ktor:ktor-client-core-jvm:2.3.5")
    implementation("io.ktor:ktor-network-tls-certificates-jvm:2.3.5")
    implementation("io.ktor:ktor-client-okhttp-jvm:2.3.5")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.5")

    //serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("net.mamoe.yamlkt:yamlkt-jvm:0.12.0")

    implementation("org.jsoup:jsoup:1.15.3")

    //logging
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("org.apache.logging.log4j:log4j-core:${Versions.log4j}")
    implementation("org.apache.logging.log4j:log4j-api:${Versions.log4j}")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:${Versions.log4j}")
    implementation("com.lmax:disruptor:3.4.4")

    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib-jdk8"))
}

//kotlin {
//    sourceSets.all {
//        languageSettings {
//            languageVersion = "2.0"
//        }
//    }
//}

tasks.test {
    useJUnit()
}

val main = "$group.BgmApplicationKt"

application {
    mainClass.set(main)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
        )
        javaParameters = true
    }
}
kotlin {
    jvmToolchain(17)
}