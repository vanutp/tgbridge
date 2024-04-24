import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("dev.architectury.loom")
}

repositories {
    maven("https://maven.minecraftforge.net/releases")
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
        content { includeGroup("thedarkcolour") }
    }
}

val minecraftVersion: String by project
val yarnMappings: String by project
val forgeVersion: String by project
val forgeKotlinVersion: String by project

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    forge("net.minecraftforge:forge:${minecraftVersion}-${forgeVersion}")
    mappings("net.fabricmc:yarn:${yarnMappings}:v2")

    implementation("thedarkcolour:kotlinforforge:${forgeKotlinVersion}")

    val adventureVersion = "4.15.0"
    shadow("net.kyori:adventure-api:${adventureVersion}")
    shadow("net.kyori:adventure-text-serializer-gson:${adventureVersion}") {
        exclude(module = "gson")
    }

    implementation(project(":common"))
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
        relocate("net.kyori", "tgbridge.shaded.kyori")
    }
    named<RemapJarTask>("remapJar") {
        inputFile = shadowJar.get().archiveFile
        archiveFileName = "${rootProject.name}-${rootProject.version}-${project.name}.jar"
        destinationDirectory.set(file("../build/release"))
    }
}

modrinth {
    uploadFile.set(tasks.remapJar)
    gameVersions.addAll("1.19.2")
    loaders.addAll("forge")
    dependencies {
        required.project("kotlin-for-forge")
    }
}
