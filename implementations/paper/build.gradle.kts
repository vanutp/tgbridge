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
        }
    }
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
    implementation(project(":common"))

    compileOnly("com.charleskorn.kaml:kaml:${rootProject.properties["kamlVersion"]}")

    compileOnly("com.github.LeonMangler:SuperVanish:$superVanishVersion")
    compileOnly("net.essentialsx:EssentialsX:$essentialsXVersion")
    compileOnly("ru.brikster:chatty-api:3.0.0-20240908.194144-1")
    compileOnly("ru.mrbrikster:chatty-api:2.19.13")
}


tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }

    reobfJar {
        outputJar.set(rootProject.layout.buildDirectory.file("release/${rootProject.name}-${rootProject.version}-${project.name}.jar"))
    }

    assemble {
        dependsOn(reobfJar)
    }
}

modrinth {
    uploadFile.set(tasks.reobfJar.get().outputJar)
    gameVersions.addAll(
        "1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4",
        "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4",
        "1.20.5", "1.20.6", "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4"
    )
    loaders.addAll("paper", "folia", "purpur")
    dependencies {
        optional.project("essentialsx")
    }
}
