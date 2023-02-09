import { Client, ResponseInterceptor } from "./client.js";

export class Manager {
  private client: Client;

  constructor(client: Client) {
    this.client = client;
  }

  public static create(
    kcURL: string,
    realm: string,
    accessToken: string,
    interceptors?: ResponseInterceptor[]
  ): Manager {
    return new Manager(new Client(kcURL, realm, accessToken, interceptors));
  }

  public async apply(...migrations: Array<() => Promise<any>>) {
    const result = [];
    const { version, dirty } = await this.client.getVersion();
    console.log(`migrating from migration #${version}`);
    if (dirty) {
      throw new Error(
        `current migration is dirty. Apply corrective actions and set migration version manually with manager.forceVersion(newVersion)`
      );
    }
    let finalVersion = 0;
    for (let ordinal = 1; ordinal <= migrations.length; ordinal++) {
      finalVersion = ordinal;
      if (ordinal <= version) {
        result.push(null);
        continue;
      }
      const migrate = migrations[ordinal - 1];
      try {
        result.push(await migrate());
        console.log(`migration #${version} succeed`);
      } catch (err) {
        await this.markDirty(finalVersion);
        throw err;
      }
    }
    if (finalVersion > version) {
      const migration = await this.client.setVersion(finalVersion, false);
      console.log(`arrived at migration #${migration.version}`);
    } else {
      console.log("no new migration");
    }
    return result;
  }

  public async forceVersion(version: number, dirty: boolean = false) {
    await this.client.setVersion(version, dirty);
  }

  public async getVersion() {
    return this.client.getVersion();
  }

  private async markDirty(version: number) {
    await this.client.setVersion(version, true);
    console.error(
      `migration #${version} failed. Migration is marked as dirty.`
    );
  }
}
