This module contains a Gradle Plugin, that acts as a "meta-plugin" of sorts, because it performs the following tasks:

- pulls in some other plugins EDC plugins
- pulls in some general plugins, e.g. `maven-publish`
- performs general configuration, e.g. adding standard dependencies to all projects
- applies the `groupId`
- applies all values for the signing config, e.g. developer name, etc.