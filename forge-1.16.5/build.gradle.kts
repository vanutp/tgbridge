import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("dev.architectury.loom")
}

repositories {
    maven("https://maven.minecraftforge.net/releases")
}

val minecraftVersion: String by project
val yarnMappings: String by project
val forgeVersion: String by project
val forgeKotlinVersion: String by project

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    forge("net.minecraftforge:forge:${minecraftVersion}-${forgeVersion}")
    mappings("net.fabricmc:yarn:${yarnMappings}:v2")

    implementation(project(":common"))
    shadow(project(":common"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 8
    }

    named<KotlinJvmCompile>("compileKotlin") {
        kotlinOptions.jvmTarget = "1.8"
    }

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
        destinationDirectory.set(file("../build/release"))
    }
}

modrinth {
    uploadFile.set(tasks.remapJar)
    gameVersions.addAll("1.16.5")
    loaders.addAll("forge")
}
