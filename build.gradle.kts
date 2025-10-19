import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
    id("com.gradleup.shadow") version "8.3.4"
    id("io.papermc.paperweight.userdev") version "1.7.7" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1" apply false
    id("dev.architectury.loom") version "1.11-SNAPSHOT" apply false
    id("com.modrinth.minotaur") version "2.+"
    `maven-publish`
}

group = "dev.vanutp.tgbridge"
version = property("projectVersion") as String

val kotlinVersion: String by project
val kotlinxCoroutinesVersion: String by project
val kotlinxSerializationVersion: String by project
val adventureVersion: String by project

// I didn't find a good kotlin for paper library
// kotlinforforge for 1.16.5 hasn't been updated for a long time
fun checkBundleKotlin(projectName: String) =
    listOf("paper", "forge-1.16.5").contains(projectName)

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.serialization")
        plugin("com.gradleup.shadow")
        plugin("com.modrinth.minotaur")
        plugin("maven-publish")
    }

    group = rootProject.group
    version = rootProject.version
    base.archivesName = rootProject.name + "-" + project.name

    repositories {
        mavenCentral()
    }

    dependencies {
        val KOTLIN_LIBS = listOf(
            "org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}",
            "org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}",
            "org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinxCoroutinesVersion}",
            "org.jetbrains.kotlinx:kotlinx-serialization-core:${kotlinxSerializationVersion}",
            "org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinxSerializationVersion}"
        )
        val ADVENTURE_LIBS = listOf(
            "net.kyori:adventure-api:${adventureVersion}",
            "net.kyori:adventure-text-serializer-gson:${adventureVersion}",
            "net.kyori:adventure-text-minimessage:${adventureVersion}",
        )

        (KOTLIN_LIBS + ADVENTURE_LIBS).forEach {
            testImplementation(it)
        }
        if (checkBundleKotlin(project.name)) {
            KOTLIN_LIBS.forEach { lib ->
                shadow(implementation(lib)!!)
            }
        } else {
            KOTLIN_LIBS.forEach { lib ->
                compileOnly(lib)
            }
        }

        if (project.name == "paper" || project.name == "common") {
            ADVENTURE_LIBS.forEach {
                compileOnly(it) {
                    // technically needs to be excluded only for text-serializer-gson,
                    // but idk how to do this easily
                    exclude(module = "gson")
                }
            }
        } else {
            ADVENTURE_LIBS.forEach { lib ->
                shadow(implementation(lib) {
                    exclude(module = "gson")
                })
            }
        }

        // gson is available in all loaders at runtime
        compileOnly("com.google.code.gson:gson:2.10.1")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        toolchain.languageVersion = JavaLanguageVersion.of(21)
        withSourcesJar()
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release = 17
        }

        named<ShadowJar>("shadowJar") {
            from(rootDir.resolve("LICENSE")) {
                rename { "${it}_${rootProject.name}" }
            }
            from(rootDir.resolve("LICENSE")) {
                rename { "${it}_${rootProject.name}" }
            }
            relocate("okio", "tgbridge.shaded.okio")
            relocate("okhttp3", "tgbridge.shaded.okhttp3")
            relocate("retrofit2", "tgbridge.shaded.retrofit2")

            relocate("it.krzeminski.snakeyaml", "tgbridge.shaded.snakeyaml")
            relocate("net.thauvin", "tgbridge.shaded.net.thauvin")
            relocate("com.charleskorn.kaml", "tgbridge.shaded.kaml")

            if (project.name != "paper") {
                // shadowJar is never ran for common
                relocate("net.kyori", "tgbridge.shaded.kyori")
            }
            // Renames files in META-INF/services to relocated names
            // fixes "Modules net.kyori.... and tgbridge export package net.kyori..." in forge
            mergeServiceFiles()

            if (checkBundleKotlin(project.name)) {
                relocate("kotlin", "tgbridge.shaded.kotlin")
                relocate("kotlinx", "tgbridge.shaded.kotlinx")
                relocate("org.jetbrains", "tgbridge.shaded.org.jetbrains")
                relocate("org.intellij", "tgbridge.shaded.org.intellij")
            }

            minimize()
        }
    }

    modrinth {
        val loader = project.name.replaceFirst("-", " ").replaceFirstChar { it.uppercaseChar() }
        versionName = "${project.version} ($loader)"
        token.set(System.getenv("MODRINTH_TOKEN"))
        projectId.set("tgbridge")
        versionType.set("release")
        changelog.set(
            file("$rootDir/CHANGELOG.md")
                .readText()
                .split("###")[1]
                .let { x -> "###$x" }
        )
    }

    publishing {
        if (project.name == "common" || project.name == "paper") {
            publishing {
                publications {
                    create<MavenPublication>("maven") {
                        groupId = project.group.toString()
                        artifactId = project.name
                        version = System.getenv("VERSION") ?: project.version.toString()
                        from(components["java"])
                    }
                }
            }
        }

        repositories {
            maven {
                name = "vanutp"
                url = uri("https://mvn.vtp.sh/main")
                credentials {
                    username = System.getenv("REGISTRY_USERNAME")
                    password = System.getenv("REGISTRY_TOKEN")
                }
            }
        }
    }
}

task("modrinthAll") {
    group = "publishing"
    dependsOn(":fabric:modrinth")
    dependsOn(":forge-1.16.5:modrinth")
    dependsOn(":forge-1.19.2:modrinth")
    dependsOn(":forge-1.20.1:modrinth")
    dependsOn(":neoforge-1.21:modrinth")
    dependsOn(":paper:modrinth")
}
