plugins {
    `java-gradle-plugin`
}

val group: String by project

gradlePlugin {
    website.set("https://projects.eclipse.org/projects/technology.edc")
    vcsUrl.set("https://github.com/eclipse-edc/GradlePlugins.git")
    // Define the plugin
    plugins {
        create("module-names") {
            displayName = "module-names"
            description =
                "Plugin to verify that a project has no duplicate submodules (by name)"
            id = "${group}.module-names"
            implementationClass = "org.eclipse.edc.plugins.modulenames.ModuleNamesPlugin"
            tags.set(listOf("build", "verification"))
            version = version
        }
    }
}
