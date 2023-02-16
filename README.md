# Keycloak Migration

Everyone starts out configuring Keycloak using the admin console. However, for
maintainability, any changes to Keycloak config should eventually be made via
migration scripts (possibly written with
[keycloak-admin-client](https://github.com/keycloak/keycloak-nodejs-admin-client))
persisted in a Git repo. This extension adds a REST API to Keycloak that allows
persisting migration progress.

This Java extension and the JavaScript library `keycloak-migration` act as a duo
to enable this use case.

## Installation

1. Download the Java extension from Maven, then install as an extension to
   Keycloak

   ```bash
   mvn dependency:get -Dartifact=io.teragroup.keycloak.extension:migration:20.0.3.1
   cp ~/.m2/repository/io/teragroup/keycloak/extension/migration/20.0.3.1/migration-20.0.3.1.jar ${KEYCLOAK_HOME_DIR}/providers
   ${KEYCLOAK_HOME_DIR}/bin/kc.sh build
   ```

2. Install the JavaScript library

   ```bash
   npm install @teravn/keycloak-migration axios @keycloak/keycloak-admin-client
   ```

## Usage

1. In realm master, create a confidential client named "keycloak-migration" with
   "authorization service" enabled. Note this client's secret.
2. Create one or more migration scripts:

   ```ts
   // migrations/001_create_realm.ts
   import KcAdminClient from "@keycloak/keycloak-admin-client";

   export default (kcAdminClient: KcAdminClient) => async () => {
     await kcAdminClient.realms.create({
       realm: "my-realm",
       userManagedAccessAllowed: true,
     });
   };
   ```

3. Create the entry point:

   ```ts
   // migrate.ts
   import querystring from "querystring";
   import axios from "axios";
   import KcAdminClient from "@keycloak/keycloak-admin-client";
   import { Manager } from "@teravn/keycloak-migration";

   import apply_001 from "./migrations/001_create_realm";
   import apply_002 from "./migrations/002_abc";
   import apply_003 from "./migrations/003_xyz";

   const keycloakURL = process.env.KEYCLOAK_URL;

   // this is the client secret of client "keycloak-migration"
   const clientSecret = process.env.CLIENT_SECRET;

   // keycloak admin credentials
   const adminUsername = process.env.ADMIN_USERNAME;
   const adminPassword = process.env.ADMIN_PASSWORD;

   // invoke this function to start applying migrations
   export default async () => {
     // acquire an access token using client credentials
     const axiosInst = axios.create();
     const discoveryResp = await axiosInst.get(
       `${keycloakURL}/realms/${realm}/.well-known/uma2-configuration`
     );
     const tokenResp = await axiosInst.post(
       discoveryResp.data.token_endpoint,
       querystring.stringify({
         grant_type: "client_credentials",
         client_id: "keycloak-migration",
         client_secret: clientSecret,
       })
     );

     // create the migration manager
     const manager = Manager.create(
       kcURL,
       tokenResp.data.access_token as string
     );

     // create keycloak admin client
     const kcAdminClient = new KcAdminClient({
       baseUrl: keycloakURL,
       realmName: "master",
     });

     // authenticate keycloak admin client
     await kcAdminClient.auth({
       grantType: "password",
       username: adminUsername,
       password: adminPassword,
       clientId: "admin-cli",
     });

     // apply migrations. Note that migration version is 1-based. If these
     // migrations all succeed then the final migration version will be 3.
     const results = await manager.apply(
       apply_001(kcAdminClient),
       apply_002(kcAdminClient),
       apply_003(kcAdminClient)
     );
   };
   ```

## Dealing with migration errors

If one of the migration steps rejects then the migration will be marked as
dirty. Migrations will not proceed automatically in this state. You need to fix
what is wrong with Keycloak and then call `manager.forceVersion(newVersion)`
manually.
