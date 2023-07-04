plugins {
    `java-gradle-plugin`
}

group = "org.eclipse.edc"

repositories {
    mavenCentral()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    gradlePluginPortal()
    mavenLocal()
}

dependencies {
    implementation(libs.plugin.dependency.analysis)
    implementation(libs.plugin.nexus.publish)
    implementation(libs.plugin.checksum)
    implementation(libs.plugin.swagger.generator)
    implementation(libs.plugin.swagger)
    implementation(libs.plugin.openapi.merger)
    implementation(libs.plugin.openapi.merger.app)

    implementation(libs.jetbrains.annotations)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatypeJsr310)

    api(libs.edc.runtime.metamodel)

}

gradlePlugin {
    plugins {
        create("edc-build") {
            id = "${group}.edc-build"
            implementationClass = "${group}.plugins.edcbuild.EdcBuildPlugin"
        }
    }
}

sourceSets {
    main {
        java {
            srcDirs(
                "../plugins/autodoc/autodoc-plugin/src/main",
                "../plugins/autodoc/autodoc-processor/src/main",
                "../plugins/edc-build/src/main",
                "../plugins/module-names/src/main",
                "../plugins/openapi-merger/src/main",
                "../plugins/test-summary/src/main"
            )
        }
    }
}


