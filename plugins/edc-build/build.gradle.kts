plugins {
    `java-gradle-plugin`
}

val group: String by project

repositories {
    mavenCentral()
    gradlePluginPortal() // needed because some plugins are only published to the Plugin Portal
}

dependencies {
    implementation(project(":plugins:autodoc:autodoc-plugin"))
    implementation(project(":plugins:test-summary"))
    implementation(project(":plugins:module-names"))
    implementation(project(":plugins:openapi-merger"))

    implementation(libs.edc.runtime.metamodel)
    implementation(libs.plugin.nexus.publish)
    implementation(libs.plugin.checksum)
    implementation(libs.plugin.swagger)
    implementation(libs.plugin.openapi.merger)
}

gradlePlugin {
    website.set("https://projects.eclipse.org/projects/technology.edc")
    vcsUrl.set("https://github.com/eclipse-edc/GradlePlugins.git")
    // Define the plugins
    plugins {
        create("edc-build-base") {
            displayName = "edc-build-base"
            description =
                "Meta-plugin that provides the capabilities of the EDC build"
            id = "${group}.edc-build-base"
            implementationClass = "org.eclipse.edc.plugins.edcbuild.EdcBuildBasePlugin"
            tags.set(listOf("build", "verification", "test"))
        }
        create("edc-build") {
            displayName = "edc-build"
            description =
                "Plugin that applies the base capabilities and provides default configuration for the EDC build"
            id = "${group}.edc-build"
            implementationClass = "org.eclipse.edc.plugins.edcbuild.EdcBuildPlugin"
            tags.set(listOf("build", "verification", "test"))
        }
    }
}
