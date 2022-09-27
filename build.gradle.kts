plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    checkstyle
    `maven-publish`
    signing
    `java-library`
    // for publishing to nexus/ossrh/mavencentral
    id("org.gradle.crypto.checksum") version "1.4.0"
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("com.gradle.plugin-publish") version "1.0.0" apply false
}

val groupId: String by project
val defaultVersion: String by project
val jupiterVersion: String by project
val assertj: String by project
val mockitoVersion: String by project

// values needed for publishing
val pluginsWebsiteUrl: String by project
val pluginsDeveloperId: String by project
val pluginsDeveloperName: String by project
val pluginsDeveloperEmail: String by project
val pluginsScmConnection: String by project
val pluginsScmUrl: String by project

var actualVersion: String = (project.findProperty("version") ?: defaultVersion) as String
if (actualVersion == "unspecified") {
    actualVersion = defaultVersion
}

allprojects {
    apply(plugin = "checkstyle")
    apply(plugin = "maven-publish")
    version = actualVersion
    group = groupId

    // for all gradle plugins:
    pluginManager.withPlugin("java-gradle-plugin") {
        apply(plugin = "com.gradle.plugin-publish")
    }

    // for all java libs:
    pluginManager.withPlugin("java-library") {
        if (!project.hasProperty("skip.signing")) {
            apply(plugin = "signing")

            //set the deploy-url only for java libraries
            val deployUrl =
                if (actualVersion.contains("SNAPSHOT")) "https://oss.sonatype.org/content/repositories/snapshots/"
                else "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            publishing {
                repositories {
                    maven {
                        name = "OSSRH"
                        setUrl(deployUrl)
                        credentials {
                            username = System.getenv("OSSRH_USER") ?: return@credentials
                            password = System.getenv("OSSRH_PASSWORD") ?: return@credentials
                        }
                    }
                }

                signing {
                    useGpgCmd()
                    sign(publishing.publications)
                }
            }

        }

        java {
            val javaVersion = 11
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(javaVersion))
            }
            tasks.withType(JavaCompile::class.java) {
                // making sure the code does not use any APIs from a more recent version.
                // Ref: https://docs.gradle.org/current/userguide/building_java_projects.html#sec:java_cross_compilation
                options.release.set(javaVersion)
            }
            withJavadocJar()
            withSourcesJar()
        }

        dependencies {
            // Use JUnit test framework for unit tests
            testImplementation("org.junit.jupiter:junit-jupiter-api:${jupiterVersion}")
            testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
            testImplementation("org.junit.jupiter:junit-jupiter-params:${jupiterVersion}")
            testImplementation("org.assertj:assertj-core:${assertj}")
            testImplementation("org.mockito:mockito-core:${mockitoVersion}")
            testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${jupiterVersion}")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            showStandardStreams = true
        }
    }

    // configure checkstyle version
    checkstyle {
        toolVersion = "10.0"
        maxErrors = 0 // does not tolerate errors
    }

    repositories {
        mavenCentral()
    }

    // let's not generate any reports because that is done from within the Github Actions workflow
    tasks.withType<Checkstyle> {
        reports {
            html.required.set(false)
            xml.required.set(true)
        }
    }

    afterEvaluate {
        publishing {
            publications.forEach { i ->
                val mp = (i as MavenPublication)
                mp.pom {
                    name.set(project.name)
                    description.set("edc :: ${project.name}")
                    url.set(pluginsWebsiteUrl)

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                        developers {
                            developer {
                                id.set(pluginsDeveloperId)
                                name.set(pluginsDeveloperName)
                                email.set(pluginsDeveloperEmail)
                            }
                        }
                        scm {
                            connection.set(pluginsScmConnection)
                            url.set(pluginsScmUrl)
                        }
                    }
                }
//                println("\n${mp.groupId}:${mp.artifactId}:${mp.version}")
            }
        }
    }

}

repositories {
    // Use Maven Central for resolving dependencies
    mavenCentral()
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("OSSRH_USER") ?: return@sonatype)
            password.set(System.getenv("OSSRH_PASSWORD") ?: return@sonatype)
        }
    }
}