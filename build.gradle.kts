import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.23"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.paperweight.userdev") version "1.5.15" apply false
    id("xyz.jpenilla.run-paper") version "2.2.3" apply false
    id("dev.architectury.loom") version "1.6-SNAPSHOT" apply false
    id("com.modrinth.minotaur") version "2.+"
}

group = "dev.vanutp"
version = property("projectVersion") as String

val kotlinVersion: String by project
val kotlinxCoroutinesVersion: String by project
val kotlinxSerializationVersion: String by project
val adventureVersion: String by project

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.serialization")
        plugin("com.github.johnrengelman.shadow")
        plugin("com.modrinth.minotaur")
    }

    group = rootProject.group
    version = rootProject.version
    base.archivesName = rootProject.name + "-" + project.name

    repositories {
        mavenCentral()
        maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
        exclusiveContent {
            forRepository {
                maven ("https://api.modrinth.com/maven") { name = "Modrinth" }
            }
            filter {
                includeGroup("maven.modrinth")
            }
        }
    }

    dependencies {
        if (project.name == "paper") {
            // I didn't find a good kotlin for paper library
            implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
            implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinxCoroutinesVersion}")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${kotlinxSerializationVersion}")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinxSerializationVersion}")
        } else {
            compileOnly("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
            compileOnly("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
            compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinxCoroutinesVersion}")
            compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core:${kotlinxSerializationVersion}")
            compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinxSerializationVersion}")
        }

        if (project.name == "paper" || project.name == "common") {
            compileOnly("net.kyori:adventure-api:${adventureVersion}")
            compileOnly("net.kyori:adventure-text-serializer-gson:${adventureVersion}") {
                exclude(module = "gson")
            }
        } else {
            shadow(implementation("net.kyori:adventure-api:${adventureVersion}")!!)
            shadow(implementation("net.kyori:adventure-text-serializer-gson:${adventureVersion}") {
                exclude(module = "gson")
            })
        }

        // gson is available in all loaders at runtime
        compileOnly("com.google.code.gson:gson:2.10.1")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        toolchain.languageVersion = JavaLanguageVersion.of(21)
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release = 17
        }

        named<KotlinJvmCompile>("compileKotlin") {
            kotlinOptions.jvmTarget = "17"
        }

        named<ShadowJar>("shadowJar") {
            from("LICENSE") {
                rename { "${it}_${project.base.archivesName.get()}" }
            }
            relocate("okio", "tgbridge.shaded.okio")
            relocate("okhttp3", "tgbridge.shaded.okhttp3")
            relocate("retrofit2", "tgbridge.shaded.retrofit2")
            relocate("org.snakeyaml", "tgbridge.shaded.snakeyaml")
            relocate("com.charleskorn.kaml", "tgbridge.shaded.kaml")

            if (project.name != "paper") {
                // shadowJar is never ran for common
                relocate("net.kyori", "tgbridge.shaded.kyori")
            }
            // Renames files in META-INF/services to relocated names
            // fixes "Modules net.kyori.... and tgbridge export package net.kyori..." in forge
            mergeServiceFiles()
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
}

task("publishAll") {
    group = "publishing"
    dependsOn(":fabric-1.19.2:modrinth")
    dependsOn(":fabric-1.20.1-1.20.4:modrinth")
    dependsOn(":fabric-1.20.6-1.21.3:modrinth")
    dependsOn(":forge-1.19.2:modrinth")
    dependsOn(":forge-1.20.1:modrinth")
    dependsOn(":neoforge-1.21:modrinth")
    dependsOn(":paper:modrinth")
}
