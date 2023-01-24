plugins {
    `java-gradle-plugin`
    id("org.gradle.crypto.checksum") version "1.4.0"
}

val jupiterVersion: String by project
val assertj: String by project
val groupId: String by project

gradlePlugin {
    // Define the plugin
    plugins {
        create("module-names") {
            displayName = "module-names"
            description =
                "Plugin to verify that a project has no duplicate submodules (by name)"
            id = "${groupId}.module-names"
            implementationClass = "org.eclipse.edc.plugins.modulenames.ModuleNamesPlugin"
        }
    }
}

pluginBundle {
    website = "https://projects.eclipse.org/proposals/eclipse-dataspace-connector"
    vcsUrl = "https://github.com/eclipse-dataspaceconnector/GradlePlugins.git"
    version = version
    tags = listOf("build", "verification")
}