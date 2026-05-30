import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.3.21"
    id("net.fabricmc.fabric-loom") version "1.15-SNAPSHOT"
    id("io.github.dexman545.outlet") version "1.8.+"
}

version = properties["version"] as String

repositories {
    mavenCentral()
}

dependencies {
    val gameVersion: String by properties
    outlet.mcVersionRange = properties["fabricSupportedVersions"] as String

    //
    // Fabric configuration
    //
    minecraft("com.mojang:minecraft:$gameVersion")
    println("Game Version: $gameVersion\nSupported Versions: ${outlet.mcVersionRange}")
    println("FabricLoader: ${outlet.loaderVersion()}\nFabricAPI: ${outlet.fapiVersion()}")
    implementation("net.fabricmc:fabric-loader:${outlet.loaderVersion()}")
    implementation("net.fabricmc.fabric-api:fabric-api:${outlet.fapiVersion()}")

    //
    // Kotlin libraries
    //
    val flkVersion = outlet.latestModrinthModVersion("fabric-language-kotlin", outlet.mcVersions())
    println("Fabric Language Kotlin: $flkVersion")
    implementation("net.fabricmc:fabric-language-kotlin:$flkVersion")
}

tasks.processResources {
    val modrinthSlug = properties["modrinthId"] as? String ?: properties["modid"] as String
    val expansion = mapOf(
        "modid" to properties["modid"] as String,
        "version" to properties["version"] as String,
        "name" to properties["projectName"] as String,
        "description" to properties["description"],
        "author" to properties["author"] as String,
        "license" to properties["licence"] as String,
        "modrinth" to modrinthSlug,
        "mcversion" to outlet.mcVersionRange,
        "loaderVersion" to outlet.loaderVersion()
    )
    inputs.properties(expansion)
    filesMatching("fabric.mod.json") {
        expand(expansion)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks {
    withType<AbstractArchiveTask>().configureEach {
        //archiveVersion.set("${project.version}+${properties["gameVersion"]}")
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(25)
    }
    compileKotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_25
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }
}
