package io.teragroup.keycloak.extension.migration.domainextension.jpa;

import java.util.Collections;
import java.util.List;

import org.keycloak.connections.jpa.entityprovider.JpaEntityProvider;

public class MigrationJpaEntityProvider implements JpaEntityProvider {

    @Override
    public List<Class<?>> getEntities() {
        return Collections.<Class<?>>singletonList(Migration.class);
    }

    @Override
    public String getChangelogLocation() {
        return "META-INF/migration-changelog.xml";
    }

    @Override
    public void close() {
    }

    @Override
    public String getFactoryId() {
        return MigrationJpaEntityProviderFactory.ID;
    }

}
