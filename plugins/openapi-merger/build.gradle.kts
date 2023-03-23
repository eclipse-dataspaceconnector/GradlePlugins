plugins {
    `java-gradle-plugin`
}

val groupId: String by project

dependencies {
    // contains the actual merger task
    implementation(libs.plugin.openapi.merger)
    // needed for the OpenApiDataInvalidException:
    implementation(libs.plugin.openapi.merger.app)
}

gradlePlugin {
    website.set("https://projects.eclipse.org/projects/technology.edc")
    vcsUrl.set("https://github.com/eclipse-edc/GradlePlugins.git")
    // Define the plugin
    plugins {
        create("openapi-merger") {
            displayName = "openapi-merger"
            description =
                "Plugin to several OpenAPI spec files into one"
            id = "${groupId}.openapi-merger"
            implementationClass = "org.eclipse.edc.plugins.openapimerger.OpenApiMergerPlugin"
            tags.set(listOf("build", "openapi", "merge", "documentation"))
        }
    }
}

configure<org.eclipse.edc.plugins.autodoc.AutodocExtension> {
    excluded.set(true)
}
