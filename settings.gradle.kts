pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
        name = "Fabric"
    }
    maven("https://maven.neoforged.net/releases") {
        name = "NeoForged"
    }
    gradlePluginPortal()
}

rootProject.name = "AnimatedDoors"
include("neoforge-run")
}
