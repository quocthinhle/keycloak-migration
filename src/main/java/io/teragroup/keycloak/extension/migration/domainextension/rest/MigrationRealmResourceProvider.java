package io.teragroup.keycloak.extension.migration.domainextension.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class MigrationRealmResourceProvider implements RealmResourceProvider {

    private KeycloakSession session;

    public MigrationRealmResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new MigrationRestResource(session);
    }

    @Override
    public void close() {
    }

}
