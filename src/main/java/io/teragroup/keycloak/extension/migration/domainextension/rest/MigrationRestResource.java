package io.teragroup.keycloak.extension.migration.domainextension.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AppAuthManager;
import org.keycloak.services.managers.AuthenticationManager;

import io.teragroup.keycloak.extension.migration.domainextension.MigrationRepresentation;
import io.teragroup.keycloak.extension.migration.domainextension.spi.MigrationService;

import org.jboss.resteasy.annotations.cache.NoCache;

import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
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
        checkRealmAdmin();
        return session.getProvider(MigrationService.class).getMigration();
    }

    @POST
    @Path("")
    @NoCache
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MigrationRepresentation createWebhook(MigrationRepresentation rep) {
        checkRealmAdmin();
        return session.getProvider(MigrationService.class).setMigration(rep);
    }

    /**
     * checkRealmAdmin ensures that only users with "realm-management.realm-admin"
     * role can access this resource
     */
    private void checkRealmAdmin() {
        if (auth == null) {
            throw new NotAuthorizedException("Bearer");
        } else if (auth.getToken().getRealmAccess() == null
                || !auth.getToken().getResourceAccess().get("realm-management").isUserInRole("realm-admin")) {
            throw new ForbiddenException("Does not have realm-management.realm-admin role");
        }
    }

}
