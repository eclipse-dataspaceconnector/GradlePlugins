plugins {
    `java-gradle-plugin`
    id("org.gradle.crypto.checksum") version "1.4.0"
}

val jetBrainsAnnotationsVersion: String by project
val jacksonVersion: String by project

dependencies {
    implementation("org.jetbrains:annotations:${jetBrainsAnnotationsVersion}")
    implementation("com.fasterxml.jackson.core:jackson-core:${jacksonVersion}")
    implementation("com.fasterxml.jackson.core:jackson-annotations:${jacksonVersion}")
    implementation("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")
}

val jupiterVersion: String by project
val assertj: String by project
val groupId: String by project

gradlePlugin {
    // Define the plugin
    plugins {
        create("autodoc") {
            displayName = "autodoc"
            description =
                "Plugin to generate a documentation manifest for the EDC Metamodel, i.e. extensions, SPIs, etc."
            id = "${groupId}.autodoc"
            implementationClass = "org.eclipse.edc.plugins.autodoc.AutodocPlugin"
        }
    }
}

pluginBundle {
    website = "https://projects.eclipse.org/proposals/eclipse-dataspace-connector"
    vcsUrl = "https://github.com/eclipse-dataspaceconnector/GradlePlugins.git"
    version = version
    tags = listOf("build", "documentation", "generated", "autodoc")
}