# Automatic Dependency Injection

## Decision

We will stop the automatic injection of implementation dependencies in the modules with the build plugin.

## Rationale

Automatic dependency injection will add potentially unnecessary dependencies to every module that uses the build plugin.

## Approach

Remove the automatic dependency injection for production dependency. The injection will remain for test dependencies such as Awaitility or JUnit. 
Implementation dependencies should be declared by an SPI module through the `api` configuration.
