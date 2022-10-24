# The `autodoc` Gradle Plugin

## Introduction

The `autodoc` plugin hooks into the Java compiler task (`compileJava`) and generates a module
manifest file that contains meta information about each module.
For example, it exposes all required and provided dependencies of an EDC `ServiceExtension`.

## Module structure

The `autodoc` plugin is located at `plugins/autodoc` and consists of four separate modules:

- `autodoc-plugin`: contains the actual Gradle `Plugin` and an `Extension` to configure the plugin. This module is
  published to the [Gradle Portal](https://plugins.gradle.org).
- `autodoc-processor`: contains an `AnnotationProcessor` that hooks into the compilation process and builds the manifest
  file. Published to MavenCentral.
- `autodoc-extension-test`: test code for the annotation processor. Not published.
- `autodoc-spi-test`: test code for the annotation processor. Not published.

## Usage

In order to use the `autodoc` plugin we must follow a few simple steps. All examples use the Kotlin DSL.

### Add the plugin to the `buildscript` block of your `build.gradle.kts`:

   ```kotlin
   buildscript {
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
    }
    dependencies {
        classpath("org.eclipse.edc.autodoc:org.eclipse.edc.autodoc.gradle.plugin:<VERSION>>")
    }
}
   ```

Please note that the `repositories` configuration can be omitted, if the release version of the plugin is used.

### Apply the plugin to the project:

There are two options to apply a plugin. For multi-module builds this should be done at the root level.

1. via `plugin` block:
   ```kotlin
   plugins {
       id("org.eclipse.edc.autodoc")
   }
   ```
2. using the iterative approach, useful when applying to `allprojects` or `subprojects`:
   ```kotlin
   subprojects{
      apply(plugin = "org.eclipse.edc.autodoc")
   }
   ```

### Configure the plugin [optional]

The `autodoc` plugin exposes the following configuration values:

1. the `processorVersion`: tells the plugin, which version of the annotation processor module to use. Set this value if
   the version of the plugin and of the annotation processor diverge. If this is
   omitted, the plugin will use its own version. Please enter _just_ the SemVer-compliant version string,
   no `groupId` or `artifactName` are needed.
   ```kotlin
   configure<org.eclipse.edc.plugins.autodoc.AutodocExtension> {
       processorVersion.set("<VERSION>")
   }
   ```
   **Typically, you do not need to configure this and can safely omit it.**

_The plugin will then generate an `edc.json` file for every module/gradle project._

## Merging the manifests

There is a Gradle task readily available to merge all the manifests into one large `manifest.json` file. This comes in
handy when the JSON manifest is to be converted into other formats, such as Markdown, HTML, etc.

To do that, execute the following command on a shell:

```bash
./gradlew mergeManifest
```

By default, the merged manifests are saved to `<rootProject>/build/manifest.json`. This destination file can be
configured using a task property:

```kotlin
    // delete the merged manifest before the first merge task runs
tasks.withType<MergeManifestsTask> {
    destinationFile = YOUR_MANIFEST_FILE
}
```

Be aware that due to the multithreaded nature of the merger task, every subproject's `edc.json` gets appended to the
destination file, so it is a good idea to delete that file before running the `mergeManifest` task.
Gradle can take care of that for you though:

```kotlin
// delete the merged manifest before the first merge task runs
rootProject.tasks.withType<MergeManifestsTask> {
    doFirst { YOUR_MANIFEST_FILE.delete() }
}
```