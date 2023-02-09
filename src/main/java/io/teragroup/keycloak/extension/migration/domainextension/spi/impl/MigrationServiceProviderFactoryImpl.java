package io.teragroup.keycloak.extension.migration.domainextension.spi.impl;

import org.keycloak.Config.Scope;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import io.teragroup.keycloak.extension.migration.domainextension.spi.MigrationService;
import io.teragroup.keycloak.extension.migration.domainextension.spi.MigrationServiceProviderFactory;

public class MigrationServiceProviderFactoryImpl implements MigrationServiceProviderFactory {

    @Override
    public MigrationService create(KeycloakSession session) {
        return new MigrationServiceImpl(session);
    }

    @Override
    public void init(Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "tera-migration-service-impl";
    }

}
