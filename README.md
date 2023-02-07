# Keycloak Migration

Everyone starts out configuring Keycloak using the admin console. However, for
maintainability, any changes to Keycloak config should eventually be made via
migration (possibly written with
[keycloak-admin-client](https://github.com/keycloak/keycloak-nodejs-admin-client))
scripts persisted in a Git repo. This extension adds a REST API to Keycloak that
allows persisting migration progress.
