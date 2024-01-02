package io.teragroup.keycloak.extension.migration.domainextension.spi.impl;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import io.teragroup.keycloak.extension.migration.domainextension.MigrationRepresentation;
import io.teragroup.keycloak.extension.migration.domainextension.jpa.Migration;
import io.teragroup.keycloak.extension.migration.domainextension.spi.MigrationService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;

import java.util.List;

public class MigrationServiceImpl implements MigrationService {

    private final KeycloakSession session;

    public MigrationServiceImpl(KeycloakSession session) {
        this.session = session;
        if (getRealm() == null) {
            throw new IllegalStateException("The service cannot accept a session without a realm in it's context.");
        }
    }

    private EntityManager getEntityManager() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    protected RealmModel getRealm() {
        return session.getContext().getRealm();
    }

    @Override
    public MigrationRepresentation getMigration() {
        List<Migration> entities = getEntityManager().createNamedQuery("list", Migration.class)
                .setMaxResults(1).getResultList();
        if (entities.size() <= 0) {
            return null;
        }
        return new MigrationRepresentation(entities.get(0));
    }

    @Override
    public MigrationRepresentation setMigration(MigrationRepresentation migration) {
        EntityManager em = getEntityManager();

        try {
            em.getTransaction().begin();

            Class<?> c = Migration.class;
            Table table = c.getAnnotation(Table.class);
            em.createNativeQuery("TRUNCATE TABLE " + table.name()).executeUpdate();

            Migration migrationEntity = new Migration();
            migrationEntity.setVersion(migration.getVersion());
            migrationEntity.setDirty(migration.getDirty());
            em.persist(migrationEntity);

            em.getTransaction().commit();
        } catch (Exception err) {
            em.getTransaction().rollback();
            throw err;
        }

        return migration;
    }

    public void close() {
        // Nothing to do.
    }

}
