import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
    id("com.gradleup.shadow") version "8.3.4"
    id("io.papermc.paperweight.userdev") version "1.7.7" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1" apply false
    id("dev.architectury.loom") version "1.10-SNAPSHOT" apply false
    id("com.modrinth.minotaur") version "2.+"
}

group = "dev.vanutp"
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
    }

    group = rootProject.group
    version = rootProject.version
    base.archivesName = rootProject.name + "-" + project.name

    repositories {
        mavenCentral()
    }

    dependencies {
        if (checkBundleKotlin(project.name)) {
            shadow(implementation("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")!!)
            shadow(implementation("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")!!)
            shadow(implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinxCoroutinesVersion}")!!)
            shadow(implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${kotlinxSerializationVersion}")!!)
            shadow(implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinxSerializationVersion}")!!)
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
            compileOnly("net.kyori:adventure-text-minimessage:${adventureVersion}")
        } else {
            shadow(implementation("net.kyori:adventure-api:${adventureVersion}")!!)
            shadow(implementation("net.kyori:adventure-text-serializer-gson:${adventureVersion}") {
                exclude(module = "gson")
            })
            shadow(implementation("net.kyori:adventure-text-minimessage:${adventureVersion}")!!)
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
}

task("publishAll") {
    group = "publishing"
    dependsOn(":fabric:modrinth")
    dependsOn(":forge-1.16.5:modrinth")
    dependsOn(":forge-1.19.2:modrinth")
    dependsOn(":forge-1.20.1:modrinth")
    dependsOn(":neoforge-1.21:modrinth")
    dependsOn(":paper:modrinth")
}
