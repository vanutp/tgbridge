import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
val yarnMappingsPatchVersion: String by project
val neoVersion: String by project
val forgeKotlinVersion: String by project

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    neoForge("net.neoforged:neoforge:${neoVersion}")
    mappings(loom.layered {
        mappings("net.fabricmc:yarn:${yarnMappings}:v2")
        mappings("dev.architectury:yarn-mappings-patch-neoforge:${yarnMappingsPatchVersion}")
    })

    implementation("thedarkcolour:kotlinforforge-neoforge:${forgeKotlinVersion}")

    implementation(project(":common"))
    shadow(project(":common"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

tasks {
    compileJava {
        options.release = 21
    }

    named<ProcessResources>("processResources") {
        inputs.property("version", project.version)

        filesMatching("META-INF/neoforge.mods.toml") {
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
    gameVersions.addAll("1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5")
    loaders.addAll("neoforge")
    dependencies {
        required.project("kotlin-for-forge")
    }
}
