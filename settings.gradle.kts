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
include("common")

listOf(
    "paper",
    "forge-1.16.5",
    "forge-1.19.2",
    "forge-1.20.1",
    "neoforge-1.21",
    "fabric",
).forEach {
    include(it)
    project(":$it").projectDir = file("implementations/$it")
}
