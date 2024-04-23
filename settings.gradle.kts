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
    "paper",
    "forge-1.19.2",
    "forge-1.20.1",
    "fabric-1.19.2",
    "fabric-1.20.1",
)
