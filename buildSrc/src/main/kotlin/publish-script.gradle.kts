import com.modrinth.minotaur.ModrinthExtension
import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.gradle.api.GradleException

plugins {
    id("com.modrinth.minotaur")
    id("net.darkhax.curseforgegradle")
}

afterEvaluate {
    val releaseDisplayName = "${requiredStringProperty("projectName")} - $version"
    val releaseChangelog = requiredStringProperty("changelog")
    val releaseJar = tasks.named("jar")
    val minecraftVersions = outletMinecraftVersions()

    extensions.configure<ModrinthExtension>("modrinth") {
        token.set(providers.gradleProperty("modrinthToken"))
        projectId.set(requiredStringProperty("modrinthId"))
        versionNumber.set(project.version.toString())
        versionName.set(releaseDisplayName)
        versionType.set("release")
        uploadFile.set(releaseJar)
        gameVersions.addAll(minecraftVersions)
        loaders.addAll("fabric", "neoforge", "quilt")
        changelog.set(releaseChangelog)
        detectLoaders.set(false)
    }

    tasks.register<TaskPublishCurseForge>("publishCurseForge") {
        group = "publishing"
        description = "Publishes the universal jar to CurseForge."

        val curseforgeToken = providers.gradleProperty("curseforgeToken")
        val curseforgeProjectId = providers.gradleProperty("curseforgeId")
            .orNull
            ?.takeIf { it.isNotBlank() }

        apiToken = curseforgeToken
        disableVersionDetection()

        doFirst {
            if (!curseforgeToken.isPresent || curseforgeToken.get().isBlank()) {
                throw GradleException("Missing Gradle property 'curseforgeToken'.")
            }
            if (curseforgeProjectId == null) {
                throw GradleException("Missing Gradle property 'curseforgeId'.")
            }
        }

        if (curseforgeProjectId != null) {
            upload(curseforgeProjectId, releaseJar) {
                changelog = releaseChangelog
                changelogType = "markdown"
                displayName = releaseDisplayName
                releaseType = "release"
                minecraftVersions.forEach { addGameVersion(it) }
                addModLoader("Fabric", "NeoForge", "Quilt")
                addEnvironment("Client", "Server")
            }
        }
    }

    tasks.register("publishMods") {
        group = "publishing"
        description = "Publishes the universal jar to Modrinth and CurseForge."
        dependsOn("modrinth", "publishCurseForge")
    }
}
