import { beforeAll, describe, expect, it } from "@jest/globals";
import { KeycloakAdminClient } from "@keycloak/keycloak-admin-client/lib/client.js";
import ClientRepresentation from "@keycloak/keycloak-admin-client/lib/defs/clientRepresentation.js";
import axios, { AxiosError, AxiosResponse } from "axios";
import querystring from "querystring";

import { Manager } from "../src/index.js";

function logResponse(response: AxiosResponse) {
  const { status, config } = response;
  console.log(
    `${status} ${config.method?.toUpperCase()} ${[
      config.baseURL,
      config.url
    ].join("/")}`
  );
  return response;
}

function logErrorResponse(error: AxiosError) {
  if (error.response) {
    const { status, config, headers, data } = error.response;
    console.error(
      `${status} ${config.method?.toUpperCase()} ${[
        config.baseURL,
        config.url
      ].join("/")}\n\t${
        headers["content-type"] &&
        headers["content-type"].indexOf("application/json") !== -1
          ? JSON.stringify(data)
          : ""
      }`
    );
  }
  return Promise.reject(error);
}

async function expectReject(prom: Promise<any>) {
  let obj: any = null;
  await prom.catch(err => {
    obj = err;
  });
  expect(obj).toBeTruthy();
}

describe("Manager", () => {
  const kcURL = "http://localhost:9292";
  const realm = "test-realm";

  let manager: Manager;
  let kcClient: KeycloakAdminClient;

  beforeAll(async () => {
    kcClient = new KeycloakAdminClient({
      baseUrl: kcURL,
      realmName: "master"
    });
    await kcClient.auth({
      grantType: "password",
      username: "admin",
      password: "admin",
      clientId: "admin-cli"
    });

    await kcClient.clients.create({
      realm: "master",
      id: "keycloak-migration",
      name: "keycloak-migration",
      enabled: true,
      serviceAccountsEnabled: true,
      authorizationServicesEnabled: true,
      publicClient: false,
      clientAuthenticatorType: "client-secret",
      secret: "my-secret"
    });

    console.log("login with the service account");
    const axiosInst = axios.create();
    axiosInst.interceptors.response.use(logResponse, logErrorResponse);
    const discoveryResp = await axiosInst.get(
      `${kcURL}/realms/master/.well-known/uma2-configuration`
    );
    const tokenResp = await axiosInst.post(
      discoveryResp.data.token_endpoint,
      querystring.stringify({
        grant_type: "client_credentials",
        client_id: "keycloak-migration",
        client_secret: "my-secret"
      })
    );

    manager = Manager.create(kcURL, tokenResp.data.access_token as string, {
      interceptors: [[logResponse, logErrorResponse]]
    });

    await kcClient.realms.create({
      realm,
      userManagedAccessAllowed: true,
      enabled: true
    });
    kcClient.setConfig({
      realmName: realm,
      requestArgOptions: { catchNotFound: false }
    });
  });

  it("should ensure migration is applied", async () => {
    console.log("check that no migration is set yet");
    expect(await manager.getVersion()).toEqual({ version: 0, dirty: false });

    console.log("apply 2 migrations");
    const migrations = [
      () =>
        kcClient.clients.create({
          id: "client-1"
        }),
      () => kcClient.clients.create({ id: "client-2" })
    ];
    let results = await manager.apply(...migrations);
    expect(results).toHaveLength(2);

    console.log("check that clients are created successfully");
    expect(
      await kcClient.clients.findOne({
        id: (results[0] as ClientRepresentation).id as string
      })
    ).toBeTruthy();
    expect(
      await kcClient.clients.findOne({
        id: (results[1] as ClientRepresentation).id as string
      })
    ).toBeTruthy();

    console.log("check that we are currently at migration #2");
    expect(await manager.getVersion()).toEqual({ version: 2, dirty: false });

    console.log("check that reapplying the same migration has no effect");
    results = await manager.apply(...migrations);
    expect(results).toEqual([null, null]);
    expect(await manager.getVersion()).toEqual({ version: 2, dirty: false });

    console.log("apply a failing migration");
    migrations.push(() => {
      return Promise.reject("something wrong");
    });
    await expectReject(manager.apply(...migrations));

    console.log("check that we are currently at migration #3 and it's dirty");
    expect(await manager.getVersion()).toEqual({ version: 3, dirty: true });

    console.log("check that further migration is forbidden");
    migrations[migrations.length - 1] = () =>
      kcClient.clients.create({
        id: "client-3"
      });
    await expectReject(manager.apply(...migrations));

    console.log("reset migration progress");
    await manager.forceVersion(2);

    console.log("check that migration is now possible again");
    results = await manager.apply(...migrations);
    expect(
      await kcClient.clients.findOne({
        id: (results[results.length - 1] as ClientRepresentation).id as string
      })
    ).toBeTruthy();

    console.log("check that we are currently at migration #3");
    expect(await manager.getVersion()).toEqual({ version: 3, dirty: false });
  });
});
