import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.MapProperty
import javax.inject.Inject

abstract class AnimatedDoorsCoreExtension @Inject constructor(objects: ObjectFactory) {
    val resourceDirectories: ListProperty<Any> = objects.listProperty(Any::class.java).convention(emptyList())
    val resourceExcludes: ListProperty<String> = objects.listProperty(String::class.java).convention(emptyList())
    val resourceTemplates: ListProperty<String> = objects.listProperty(String::class.java)
        .convention(listOf("fabric.mod.json", "META-INF/neoforge.mods.toml"))
    val extraResourceExpansion: MapProperty<String, Any> = objects.mapProperty(String::class.java, Any::class.java)
        .convention(emptyMap())
}

fun Project.requiredStringProperty(name: String): String {
    return providers.gradleProperty(name).orNull
        ?: findProperty(name)?.toString()
        ?: throw IllegalArgumentException("Missing Gradle property '$name'.")
}

fun Project.commonResourceExpansion(): Map<String, Any> {
    return mapOf(
        "modid" to requiredStringProperty("modid"),
        "version" to requiredStringProperty("version"),
        "name" to requiredStringProperty("projectName"),
        "description" to requiredStringProperty("description"),
        "author" to requiredStringProperty("author"),
        "license" to requiredStringProperty("licence")
    )
}

fun Project.outletMinecraftVersions(): Iterable<String> {
    val outlet = extensions.getByName("outlet")
    val method = outlet.javaClass.methods.firstOrNull { it.name == "mcVersions" && it.parameterCount == 0 }
        ?: throw IllegalStateException("Outlet extension does not expose mcVersions().")

    val versions = method.invoke(outlet)
    @Suppress("UNCHECKED_CAST")
    return versions as Iterable<String>
}
