This module contains a Gradle Plugin, that acts as a "meta-plugin" of sorts, because it performs the following tasks:

- pulls in some other plugins EDC plugins
- pulls in some general plugins, e.g. `maven-publish`. There is a way to specify the Maven repository through a couple
  of System properties:
    - `edc.publish.url`: the URL of the Maven repo
    - `edc.publish.repoName`: the name of the repo. Relevant for the `publish...` command
    - `edc.publish.username`: the repo username
    - `edc.publish.password`: the repo password
      Thus, the target repo for publication could be specified as follows:
    ```bash
    ./gradlew  -Dedc.publish.repoName=foobar \                                                                                                                                                                                                        
     -Dedc.publish.url="http://localhost:8081/repository/maven-snapshots/" \
     -Dedc.publish.user=admin \
     -Dedc.publish.password=admin \
     publishAllPublicationsToFoobar
    ```
  Note that the `edc.publish.repoName=foobar` is important, because it will define actual
  command `publishAllPublicationsToFoobar`
- performs general configuration, e.g. adding standard dependencies to all projects
- applies all values for the signing config, e.g. developer name, etc.
