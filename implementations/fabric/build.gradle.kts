import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("net.fabricmc.fabric-loom") version "1.14-SNAPSHOT"
}

val minecraftVersion: String by project
val fabricLoaderVersion: String by project
val fabricApiVersion: String by project
val fabricKotlinVersion: String by project
val vanishVersion: String by project

repositories {
    maven("https://api.modrinth.com/maven")
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    implementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")

    implementation("net.fabricmc.fabric-api:fabric-api:${fabricApiVersion}")
    implementation("net.fabricmc:fabric-language-kotlin:${fabricKotlinVersion}")

    api(project(":common"))
    shadow(project(":common"))
    implementation(project(":common-jvm21"))
    shadow(project(":common-jvm21"))
}

loom {
    accessWidenerPath = file("src/main/resources/tgbridge.accesswidener")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
    }
}

tasks {
    compileJava {
        options.release = 25
    }

    named<ProcessResources>("processResources") {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }

    jar {
        enabled = false
    }

    shadowJar {
        dependsOn("processResources")

        from(sourceSets.main.get().output.classesDirs)
        from(sourceSets.main.get().output.resourcesDir)

        configurations = listOf(project.configurations.shadow.get())
        archiveFileName = "${rootProject.name}-${rootProject.version}-${project.name}.jar"
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("release"))
    }

    assemble {
        dependsOn(shadowJar)
    }
}

modrinth {
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(
        "21.6-snapshot-4"
    )
    loaders.add("fabric")
    dependencies {
        required.project("fabric-api")
        required.project("fabric-language-kotlin")
    }
}
