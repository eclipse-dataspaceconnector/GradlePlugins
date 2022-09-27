plugins {
    `java-library`
    `maven-publish`
}

val jetBrainsAnnotationsVersion: String by project
val jacksonVersion: String by project

dependencies {

    api("org.jetbrains:annotations:${jetBrainsAnnotationsVersion}")
    api("com.fasterxml.jackson.core:jackson-core:${jacksonVersion}")
    api("com.fasterxml.jackson.core:jackson-annotations:${jacksonVersion}")
    api("com.fasterxml.jackson.core:jackson-databind:${jacksonVersion}")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")

}

tasks.test {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}

publishing {
    publications {
        create<MavenPublication>("runtime-metamodel") {
            artifactId = "runtime-metamodel"
            from(components["java"])
        }
    }
}