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


//
// Pack Building
//
val woodTypes = listOf("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "pale_oak", "bamboo", "crimson", "warped")
val extraTypes = listOf("iron", "copper", "exposed_copper", "weathered_copper", "oxidized_copper", "waxed_copper", "waxed_exposed_copper", "waxed_weathered_copper", "waxed_oxidized_copper")
val allTypes = woodTypes + extraTypes
val waxedTrapdoorModelRedirects = mapOf(
    "waxed_copper" to "copper",
    "waxed_exposed_copper" to "exposed_copper",
    "waxed_weathered_copper" to "weathered_copper",
    "waxed_oxidized_copper" to "oxidized_copper"
)
val trapdoorModelTypes = allTypes - waxedTrapdoorModelRedirects.keys

val trapdoorModelStates = listOf("bottom", "top", "open_up", "open_down")

fun trapdoorBlockstate(type: String): String {
    val modelType = waxedTrapdoorModelRedirects[type] ?: type
    val variants = listOf(
        """		"facing=north,half=bottom,open=false": { "model": "animated_doors:block/${modelType}_trapdoor_bottom" }""",
        """		"facing=south,half=bottom,open=false": { "model": "animated_doors:block/${modelType}_trapdoor_bottom", "y": 180 }""",
        """		"facing=east,half=bottom,open=false": { "model": "animated_doors:block/${modelType}_trapdoor_bottom", "y": 90 }""",
        """		"facing=west,half=bottom,open=false": { "model": "animated_doors:block/${modelType}_trapdoor_bottom", "y": 270 }""",
        """		"facing=north,half=top,open=false": { "model": "animated_doors:block/${modelType}_trapdoor_top" }""",
        """		"facing=south,half=top,open=false": { "model": "animated_doors:block/${modelType}_trapdoor_top", "y": 180 }""",
        """		"facing=east,half=top,open=false": { "model": "animated_doors:block/${modelType}_trapdoor_top", "y": 90 }""",
        """		"facing=west,half=top,open=false": { "model": "animated_doors:block/${modelType}_trapdoor_top", "y": 270 }""",
        """		"facing=north,half=bottom,open=true": { "model": "animated_doors:block/${modelType}_trapdoor_open_up" }""",
        """		"facing=south,half=bottom,open=true": { "model": "animated_doors:block/${modelType}_trapdoor_open_up", "y": 180 }""",
        """		"facing=east,half=bottom,open=true": { "model": "animated_doors:block/${modelType}_trapdoor_open_up", "y": 90 }""",
        """		"facing=west,half=bottom,open=true": { "model": "animated_doors:block/${modelType}_trapdoor_open_up", "y": 270 }""",
        """		"facing=north,half=top,open=true": { "model": "animated_doors:block/${modelType}_trapdoor_open_down" }""",
        """		"facing=south,half=top,open=true": { "model": "animated_doors:block/${modelType}_trapdoor_open_down", "y": 180 }""",
        """		"facing=east,half=top,open=true": { "model": "animated_doors:block/${modelType}_trapdoor_open_down", "y": 90 }""",
        """		"facing=west,half=top,open=true": { "model": "animated_doors:block/${modelType}_trapdoor_open_down", "y": 270 }"""
    ).joinToString(",\n")

    return """
{
	"variants": {
$variants
	}
}
""".trimIndent() + "\n"
}

fun trapdoorModel(type: String, state: String): String {
    return """
{
	"parent": "animated_doors:block/template/trapdoor_$state",
	"textures": {
		"texture": "minecraft:block/${type}_trapdoor"
	}
}
""".trimIndent() + "\n"
}

tasks.register("generateTrapdoorResources") {
    group = "resource generation"
    description = "Generates vanilla trapdoor blockstates and AnimatedDoors trapdoor model wrappers for allTypes."

    val blockstatesDir = layout.projectDirectory.dir("src/main/resources/assets/minecraft/blockstates")
    val modelsDir = layout.projectDirectory.dir("src/main/resources/assets/animated_doors/models/block")

    outputs.files(allTypes.map { blockstatesDir.file("${it}_trapdoor.json") })
    outputs.files(trapdoorModelTypes.flatMap { type -> trapdoorModelStates.map { state -> modelsDir.file("${type}_trapdoor_$state.json") } })

    doLast {
        val blockstates = blockstatesDir.asFile
        val models = modelsDir.asFile
        blockstates.mkdirs()
        models.mkdirs()

        allTypes.forEach { type ->
            blockstates.resolve("${type}_trapdoor.json").writeText(trapdoorBlockstate(type))
        }
        trapdoorModelTypes.forEach { type ->
            trapdoorModelStates.forEach { state ->
                models.resolve("${type}_trapdoor_$state.json").writeText(trapdoorModel(type, state))
            }
        }
        waxedTrapdoorModelRedirects.keys.forEach { type ->
            trapdoorModelStates.forEach { state ->
                models.resolve("${type}_trapdoor_$state.json").delete()
            }
        }
    }
}
