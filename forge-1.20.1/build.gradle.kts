import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

plugins {
    id("dev.architectury.loom")
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
val yarnMappings: String by project
val neoVersion: String by project
val forgeKotlinVersion: String by project

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    forge("net.neoforged:forge:${minecraftVersion}-${neoVersion}")
    mappings("net.fabricmc:yarn:${yarnMappings}:v2")

    implementation("thedarkcolour:kotlinforforge:${forgeKotlinVersion}")

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
        exclude("META-INF/services/net.kyori.adventure.text.serializer.json.JSONComponentSerializer\$Provider")
    }
    named<RemapJarTask>("remapJar") {
        inputFile = shadowJar.get().archiveFile
        archiveFileName = "${rootProject.name}-${rootProject.version}-${project.name}.jar"
        destinationDirectory.set(file("../build/release"))
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
