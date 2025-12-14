import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper")
}

val superVanishVersion: String by project
val essentialsXVersion: String by project

repositories {
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
        content {
            includeGroup("com.github.LeonMangler")
        }
    }
    maven {
        name = "essentialsx"
        url = uri("https://repo.essentialsx.net/releases/")
        content {
            includeGroup("net.essentialsx")
        }
    }
    maven {
        name = "scarsz"
        url = uri("https://nexus.scarsz.me/content/groups/public/")
        content {
            includeGroup("ru.brikster")
            includeGroup("ru.mrbrikster")
            includeGroup("com.dthielke.herochat")
            includeGroup("com.discordsrv")
        }
    }
    maven {
        url = uri("https://maven.maxhenkel.de/repository/public")
        content {
            includeGroup("de.maxhenkel.voicechat")
        }
    }
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
    api(project(":common"))
    implementation(project(":common-jvm21"))

    compileOnly("com.charleskorn.kaml:kaml:${rootProject.properties["kamlVersion"]}")

    compileOnly("com.github.LeonMangler:SuperVanish:$superVanishVersion")
    compileOnly("net.essentialsx:EssentialsX:$essentialsXVersion")
    compileOnly("ru.brikster:chatty-api:3.0.0-20240908.194144-1")
    compileOnly("ru.mrbrikster:chatty-api:2.19.13")
    compileOnly("de.maxhenkel.voicechat:voicechat-api:2.5.31")
    compileOnly("com.dthielke.herochat:Herochat:5.6.5")
    compileOnly("com.discordsrv:discordsrv:1.28.0")
}

// Using Mojmap here because we are building for jvm 21,
// but paper 1.19.2 has old asm dependency that can't read jvm 21 classes,
// so it can't remap our jar.
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        archiveFileName = "${rootProject.name}-${rootProject.version}-${project.name}.jar"
        destinationDirectory.set(rootProject.layout.buildDirectory.dir("release"))
    }

    assemble {
        dependsOn(shadowJar)
    }

    runServer {
        minecraftVersion("1.21.9")
    }

    withType<JavaCompile> {
        options.release = 21
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

modrinth {
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(
        "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4",
        "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4",
        "1.20.5", "1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10"
    )
    loaders.addAll("paper", "folia", "purpur")
    dependencies {
        required.project("kotlinmc")
    }
}
