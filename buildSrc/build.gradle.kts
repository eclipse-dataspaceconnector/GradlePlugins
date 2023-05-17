plugins {
    `java-gradle-plugin`
}

group = "org.eclipse.edc"

repositories {
    mavenCentral()
    gradlePluginPortal()
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
                "../runtime-metamodel/src/main",
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


