package io.teragroup.keycloak.extension.migration.domainextension.spi;

import org.keycloak.provider.Provider;
import io.teragroup.keycloak.extension.migration.domainextension.MigrationRepresentation;

public interface MigrationService extends Provider {

    MigrationRepresentation getMigration();

    MigrationRepresentation setMigration(MigrationRepresentation migration);

}
