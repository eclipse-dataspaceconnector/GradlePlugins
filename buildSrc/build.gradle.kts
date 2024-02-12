import java.nio.file.Files

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
    implementation(libs.plugin.nexus.publish)
    implementation(libs.plugin.checksum)
    implementation(libs.plugin.swagger)
    implementation(libs.plugin.openapi.merger)
    implementation(libs.plugin.openapi.merger.app)

    implementation(libs.jetbrains.annotations)
    implementation(libs.jackson.core)
    implementation(libs.jackson.annotations)
    implementation(libs.jackson.databind)
    implementation(libs.jackson.datatypeJsr310)

    api(libs.edc.runtime.metamodel)
    implementation(libs.markdown.gen)
}

gradlePlugin {
    plugins {
        create("edc-build") {
            id = "${group}.edc-build"
            implementationClass = "${group}.plugins.edcbuild.EdcBuildPlugin"
        }
    }
}

val generatedSourcesFolder = layout.buildDirectory.asFile.get().resolve("generated").resolve("sources");

sourceSets {
    main {
        java {
            srcDirs(
                generatedSourcesFolder,
                "../plugins/autodoc/autodoc-plugin/src/main",
                "../plugins/autodoc/autodoc-converters/src/main",
                "../plugins/edc-build/src/main",
                "../plugins/module-names/src/main",
                "../plugins/openapi-merger/src/main",
                "../plugins/test-summary/src/main"
            )
        }
    }
}

val createVersions = tasks.register("createVersions") {
    val folder = generatedSourcesFolder.resolve("java")
        .resolve("org").resolve("eclipse").resolve("edc").resolve("plugins")
        .resolve("edcbuild")
    folder.mkdirs()

    val versionsClassFile = folder.resolve("Versions.java");
    outputs.file(versionsClassFile)

    doLast {
        versionCatalogs.find("libs")
            .ifPresent { catalog ->
                val head = "package org.eclipse.edc.plugins.edcbuild;\npublic interface Versions {\n"
                val tail = "\n}";

                val constants = listOf("assertj", "checkstyle", "jupiter", "mockito")
                    .mapNotNull { name ->
                        catalog.findVersion(name)
                            .map { version -> "    String %s = \"%s\";".format(name.uppercase(), version) }
                            .orElse(null)
                    }
                    .joinToString("\n", head, tail)

                Files.writeString(versionsClassFile.toPath(), constants)
            }
    }

}

tasks.compileJava {
    dependsOn(createVersions)
}
