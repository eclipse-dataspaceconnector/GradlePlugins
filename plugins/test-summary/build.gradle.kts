plugins {
    `java-gradle-plugin`
}

val group: String by project

gradlePlugin {
    website.set("https://projects.eclipse.org/projects/technology.edc")
    vcsUrl.set("https://github.com/eclipse-edc/GradlePlugins.git")

    // Define the plugin
    plugins {
        create("test-summary") {
            displayName = "test-summary"
            description =
                "Plugin to verify that a project has no duplicate submodules (by name)"
            id = "${group}.test-summary"
            implementationClass = "org.eclipse.edc.plugins.testsummary.TestSummaryPlugin"
            tags.set(listOf("build", "verification", "test"))
        }
    }
}

dependencies {
    implementation(libs.edc.runtime.metamodel)
}