import java.nio.file.Files
import java.time.LocalDateTime

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
    implementation(libs.jackson.databind)

    api(libs.edc.runtime.metamodel)
    implementation(libs.markdown.gen)
    implementation(libs.j2html)
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
                val copyright = """
                    /*
                     *  Copyright (c) ${LocalDateTime.now().year} Contributors to the Eclipse Foundation
                     *
                     *  This program and the accompanying materials are made available under the
                     *  terms of the Apache License, Version 2.0 which is available at
                     *  https://www.apache.org/licenses/LICENSE-2.0
                     *
                     *  SPDX-License-Identifier: Apache-2.0
                     *
                     *  Contributors:
                     *       Contributors to the Eclipse Foundation - initial API and implementation
                     *
                     */
                """.trimIndent()

                val head = "$copyright\n\npackage org.eclipse.edc.plugins.edcbuild;\npublic interface Versions {\n"
                val tail = "\n}";

                val constants = listOf("assertj", "checkstyle", "jakarta-ws-rs", "jupiter", "mockito", "swagger")
                    .mapNotNull { name ->
                        val constantName = name.uppercase().replace("-", "_")
                        catalog.findVersion(name)
                            .map { version -> "    String %s = \"%s\";".format(constantName, version) }
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
