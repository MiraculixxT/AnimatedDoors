plugins {
    id("net.neoforged.moddev") version "2.0.141"
    id("core-script")
}

extensions.configure<AnimatedDoorsCoreExtension>("animatedDoorsCore") {
    resourceExcludes.add("fabric.mod.json")
    resourceTemplates.set(listOf("META-INF/neoforge.mods.toml"))
    extraResourceExpansion.put("neoForgeVersion", rootProject.properties["neoForgeVersion"] as String)
    extraResourceExpansion.put("neoForgeLoaderVersion", rootProject.properties["neoForgeLoaderVersion"] as String)
    extraResourceExpansion.put("neoForgeSupportedVersions", rootProject.properties["neoForgeSupportedVersions"] as String)
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
        }
    }
}
