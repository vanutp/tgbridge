import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

val kotlinVersion = "1.9.23"
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
version = "0.4.5"

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
        val kotlinxCoroutinesVersion = "1.7.3"
        val kotlinxSerializationVersion = "1.6.2"
        compileOnly("org.jetbrains.kotlin:kotlin-stdlib:${kotlinVersion}")
        compileOnly("org.jetbrains.kotlin:kotlin-reflect:${kotlinVersion}")
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:${kotlinxCoroutinesVersion}")
        compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core:${kotlinxSerializationVersion}")
        compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:${kotlinxSerializationVersion}")

        val adventureVersion = "4.17.0"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        toolchain.languageVersion = JavaLanguageVersion.of(21)
    }

    tasks {
        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release = 21
        }

        named<KotlinJvmCompile>("compileKotlin") {
            kotlinOptions.jvmTarget = "21"
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
    dependsOn(":fabric-1.20.6-1.21.1:modrinth")
}
