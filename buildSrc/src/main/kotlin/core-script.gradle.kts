import org.gradle.api.tasks.SourceSetContainer
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
    java
}

version = requiredStringProperty("version")
group = requiredStringProperty("group")

val animatedDoorsCore = extensions.create<AnimatedDoorsCoreExtension>("animatedDoorsCore")

repositories {
    mavenCentral()
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(25)
}

afterEvaluate {
    val expansion = commonResourceExpansion() + animatedDoorsCore.extraResourceExpansion.get()

    extensions.configure<SourceSetContainer>("sourceSets") {
        named("main") {
            resources {
                animatedDoorsCore.resourceDirectories.get().forEach { srcDir(it) }
                animatedDoorsCore.resourceExcludes.get().forEach { exclude(it) }
            }
        }
    }

    tasks.named<ProcessResources>("processResources") {
        inputs.properties(expansion)
        filesMatching(animatedDoorsCore.resourceTemplates.get()) {
            expand(expansion)
        }
    }
}
