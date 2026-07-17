import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("dev.architectury.loom") version "1.13-SNAPSHOT"
}

loom {
    forge {
        mixinConfig("tgbridge.mixins.json")
    }
}

repositories {
    maven("https://maven.neoforged.net/releases")
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
        content { includeGroup("thedarkcolour") }
    }
}

val minecraftVersion: String by project
val neoVersion: String by project
val forgeKotlinVersion: String by project

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    forge("net.neoforged:forge:${minecraftVersion}-${neoVersion}")
    mappings(loom.officialMojangMappings())

    implementation("thedarkcolour:kotlinforforge:${forgeKotlinVersion}")

    api(project(":common"))
    shadow(project(":common"))
}

tasks {
    named<ProcessResources>("processResources") {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand("version" to project.version)
        }
    }
    named<ShadowJar>("shadowJar") {
        dependsOn("processResources")
        finalizedBy("remapJar")

        configurations = listOf(project.configurations.shadow.get())
    }
    named<RemapJarTask>("remapJar") {
        inputFile = shadowJar.get().archiveFile
        archiveFileName = "${rootProject.name}-${rootProject.version}-${project.name}.jar"
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("release"))
    }
}

modrinth {
    uploadFile.set(tasks.remapJar)
    gameVersions.addAll("1.20.1")
    loaders.addAll("forge", "neoforge")
    dependencies {
        required.project("kotlin-for-forge")
    }
}
