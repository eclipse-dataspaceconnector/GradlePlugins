plugins {
    `java-gradle-plugin`
}

val group: String by project

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
            id = "${group}.openapi-merger"
            implementationClass = "org.eclipse.edc.plugins.openapimerger.OpenApiMergerPlugin"
            tags.set(listOf("build", "openapi", "merge", "documentation"))
        }
    }
}
