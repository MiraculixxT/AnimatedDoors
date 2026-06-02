plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("com.modrinth.minotaur:com.modrinth.minotaur.gradle.plugin:2.9.0")
    implementation("net.darkhax.curseforgegradle:net.darkhax.curseforgegradle.gradle.plugin:1.3.32")
}
