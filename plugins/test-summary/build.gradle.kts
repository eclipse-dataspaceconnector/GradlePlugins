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
        create("test-summary") {
            displayName = "test-summary"
            description =
                "Plugin to verify that a project has no duplicate submodules (by name)"
            id = "${groupId}.test-summary"
            implementationClass = "org.eclipse.edc.plugins.testsummary.TestSummaryPlugin"
        }
    }
}

pluginBundle {
    website = "https://projects.eclipse.org/proposals/eclipse-dataspace-connector"
    vcsUrl = "https://github.com/eclipse-dataspaceconnector/GradlePlugins.git"
    version = version
    tags = listOf("build", "verification", "test")
}