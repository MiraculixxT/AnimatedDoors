plugins {
    java
    id("net.neoforged.moddev") version "2.0.141"
}

version = rootProject.version
group = rootProject.properties["group"] as String

repositories {
    mavenCentral()
    maven("https://maven.neoforged.net/releases") {
        name = "NeoForged"
    }
}

neoForge {
    version = rootProject.properties["neoForgeVersion"] as String

    runs {
        register("neoForgeClient") {
            client()
            gameDirectory = rootProject.layout.projectDirectory.dir("run")
        }
    }

    mods {
        register(rootProject.properties["modid"] as String) {
            sourceSet(sourceSets.main.get())
        }
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf(rootProject.layout.projectDirectory.dir("src/main/java")))
            exclude("de/miraculixx/animated_doors/client/AnimatedDoors.java")
            exclude("de/miraculixx/animated_doors/client/config/AnimatedDoorsModMenu.java")
        }
        resources {
            setSrcDirs(listOf(rootProject.layout.projectDirectory.dir("src/main/resources")))
            exclude("fabric.mod.json")
        }
    }
}

tasks.processResources {
    val expansion = mapOf(
        "modid" to rootProject.properties["modid"] as String,
        "version" to rootProject.properties["version"] as String,
        "name" to rootProject.properties["projectName"] as String,
        "description" to rootProject.properties["description"] as String,
        "author" to rootProject.properties["author"] as String,
        "license" to rootProject.properties["licence"] as String,
        "neoForgeVersion" to rootProject.properties["neoForgeVersion"] as String,
        "neoForgeLoaderVersion" to rootProject.properties["neoForgeLoaderVersion"] as String,
        "neoForgeSupportedVersions" to rootProject.properties["neoForgeSupportedVersions"] as String
    )
    inputs.properties(expansion)
    filesMatching("META-INF/neoforge.mods.toml") {
        expand(expansion)
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release.set(25)
}
