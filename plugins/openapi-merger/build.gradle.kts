plugins {
    `java-gradle-plugin`
    id("org.gradle.crypto.checksum") version "1.4.0"
}

val jupiterVersion: String by project
val assertj: String by project
val groupId: String by project

dependencies {
    // contains the actual merger task
    implementation("com.rameshkp:openapi-merger-gradle-plugin:1.0.4")
    // needed for the OpenApiDataInvalidException:
    implementation("com.rameshkp:openapi-merger-app:1.0.4")
}

gradlePlugin {
    // Define the plugin
    plugins {
        create("openapi-merger") {
            displayName = "openapi-merger"
            description =
                "Plugin to several OpenAPI spec files into one"
            id = "${groupId}.openapi-merger"
            implementationClass = "org.eclipse.edc.plugins.openapimerger.OpenApiMergerPlugin"
        }
    }
}

pluginBundle {
    website = "https://projects.eclipse.org/proposals/eclipse-dataspace-connector"
    vcsUrl = "https://github.com/eclipse-dataspaceconnector/GradlePlugins.git"
    version = version
    tags = listOf("build", "openapi", "merge", "documentation")
}