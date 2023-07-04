plugins {
    `java-gradle-plugin`
}

dependencies {
    implementation(libs.edc.runtime.metamodel)
    implementation(libs.jetbrains.annotations)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatypeJsr310)
}

val group: String by project

gradlePlugin {
    website.set("https://projects.eclipse.org/projects/technology.edc")
    vcsUrl.set("https://github.com/eclipse-edc/GradlePlugins.git")

    // Define the plugin
    plugins {
        create("autodoc") {
            displayName = "autodoc"
            description =
                "Plugin to generate a documentation manifest for the EDC Metamodel, i.e. extensions, SPIs, etc."
            id = "${group}.autodoc"
            implementationClass = "org.eclipse.edc.plugins.autodoc.AutodocPlugin"
            tags.set(listOf("build", "documentation", "generated", "autodoc"))
        }
    }
}
