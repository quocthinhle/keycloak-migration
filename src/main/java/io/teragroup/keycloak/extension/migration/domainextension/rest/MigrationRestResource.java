package io.teragroup.keycloak.extension.migration.domainextension.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;

import io.teragroup.keycloak.extension.migration.domainextension.MigrationRepresentation;
import io.teragroup.keycloak.extension.migration.domainextension.spi.MigrationService;

import org.jboss.resteasy.annotations.cache.NoCache;

import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public class MigrationRestResource {

    private final KeycloakSession session;
    private final AuthenticationManager.AuthResult auth;

    public MigrationRestResource(KeycloakSession session) {
        this.session = session;
        this.auth = new AppAuthManager.BearerTokenAuthenticator(session).authenticate();
    }

    @GET
    @Path("")
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public MigrationRepresentation getMigration() {
        validateRealm();
        checkAccess();
        return session.getProvider(MigrationService.class).getMigration();
    }

    @POST
    @Path("")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MigrationRepresentation createWebhook(MigrationRepresentation rep) {
        validateRealm();
        checkAccess();
        return session.getProvider(MigrationService.class).setMigration(rep);
    }

    private void validateRealm() {
        RealmModel realm = session.getContext().getRealm();
        if (realm.getId() == "master") {
            throw new NotFoundException("Only available in master realm");
        }
    }

    /**
     * checkRealmAdmin ensures that only users with
     * "keycloak-migration.uma_protection"
     * role can access this resource
     */
    private void checkAccess() {
        if (auth == null) {
            throw new NotAuthorizedException("Bearer");
        } else if (auth.getToken().getRealmAccess() == null
                || !auth.getToken().getResourceAccess().get("keycloak-migration").isUserInRole("uma_protection")) {
            throw new ForbiddenException("Does not have keycloak-migration.uma_protection role");
        }
    }

}
