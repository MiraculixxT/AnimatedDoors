plugins {
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
    id("io.github.dexman545.outlet") version "1.8.+"
    id("core-script")
    id("pack-script")
    id("publish-script")
}

outlet.mcVersionRange = properties["fabricSupportedVersions"] as String

extensions.configure<AnimatedDoorsCoreExtension>("animatedDoorsCore") {
    resourceDirectories.add(layout.projectDirectory.dir("pack").asFile)

    val modrinthSlug = properties["modrinthId"] as? String ?: properties["modid"] as String
    extraResourceExpansion.put("modrinth", modrinthSlug)
    extraResourceExpansion.put("mcversion", outlet.mcVersionRange)
    extraResourceExpansion.put("loaderVersion", outlet.loaderVersion())
    extraResourceExpansion.put("neoForgeVersion", properties["neoForgeVersion"] as String)
    extraResourceExpansion.put("neoForgeLoaderVersion", properties["neoForgeLoaderVersion"] as String)
    extraResourceExpansion.put("neoForgeSupportedVersions", properties["neoForgeSupportedVersions"] as String)
}

repositories {
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/releases")
    }
}

dependencies {
    val gameVersion: String by properties

    //
    // Fabric configuration
    //
    minecraft("com.mojang:minecraft:$gameVersion")
    println("Game Version: $gameVersion\nSupported Versions: ${outlet.mcVersionRange}")
    println("FabricLoader: ${outlet.loaderVersion()}\nFabricAPI: ${outlet.fapiVersion()}")
    implementation("net.fabricmc:fabric-loader:${outlet.loaderVersion()}")
    implementation("net.fabricmc.fabric-api:fabric-api:${outlet.fapiVersion()}")
    compileOnly("com.terraformersmc:modmenu:18.0.0-beta.1")
    runtimeOnly("com.terraformersmc:modmenu:18.0.0-beta.1")

    //
    // NeoForge compile stubs for the universal sources
    //
    compileOnly("net.neoforged:neoforge:${properties["neoForgeVersion"]}")
    compileOnly("net.neoforged.fancymodloader:loader:11.0.13")
    compileOnly("net.neoforged:bus:8.0.5")
    compileOnly("net.neoforged:mergetool:2.0.7:api")
}

tasks.register("buildUniversal") {
    group = "build"
    description = "Builds the universal Fabric/NeoForge jar."
    dependsOn(tasks.build)
}

tasks.register("runNeoForgeClient") {
    group = "fabric"
    description = "Runs a NeoForge client using the shared mod sources."
    dependsOn(":neoforge-run:runNeoForgeClient")
}
