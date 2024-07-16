plugins {
    id("io.papermc.paperweight.userdev")
    id("xyz.jpenilla.run-paper")
}

repositories {
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
    implementation(project(":common"))
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
        "1.19.2", "1.19.3", "1.19.4",
        "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4",
        "1.20.5", "1.20.6", "1.21"
    )
    loaders.add("paper")
}
