/*
 *  Copyright (c) 2022 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 */

plugins {
    `java-library`
}

dependencies {
    implementation(project(":runtime-metamodel"))
    annotationProcessor(project(":plugins:autodoc:autodoc-processor"))

}

tasks.withType<JavaCompile> {
    val compilerArgs = options.compilerArgs
    compilerArgs.add("-Aedc.version=${project.version}")
    compilerArgs.add("-Aedc.id=${project.group}:${project.name}")
    compilerArgs.add("-Aedc.outputdir=${project.projectDir.absolutePath}/build")
    outputs.upToDateWhen { false }
}