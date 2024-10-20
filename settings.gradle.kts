pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven("https://maven.architectury.dev/")
        // required for architectury
        maven("https://maven.neoforged.net/releases")
    }
}

rootProject.name = "tgbridge"
include(
    "common",
    "fabric-1.20.6-1.21.1",
)
