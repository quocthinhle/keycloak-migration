import { Client, ResponseInterceptor } from "./client.js";

export interface Logger {
  log: (message: string) => void;
  error: (message: string) => void;
}

export interface ManagerOptions {
  interceptors?: ResponseInterceptor[];
  logger?: Logger;
}

export class Manager {
  private client: Client;
  private logger: Logger;

  constructor(client: Client, logger?: Logger) {
    this.client = client;
    if (logger) {
      this.logger = logger;
    } else {
      this.logger = console;
    }
  }

  public static create(
    kcURL: string,
    accessToken: string,
    options?: ManagerOptions
  ): Manager {
    return new Manager(
      new Client(kcURL, accessToken, options?.interceptors),
      options?.logger
    );
  }

  public async apply(...migrations: Array<() => Promise<any>>) {
    const result = [];
    const { version, dirty } = await this.client.getVersion();
    this.logger.log(`migrating from migration #${version}`);
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
        this.logger.log(`migration #${version} succeed`);
      } catch (err) {
        await this.markDirty(finalVersion);
        throw err;
      }
    }
    if (finalVersion > version) {
      const migration = await this.client.setVersion(finalVersion, false);
      this.logger.log(`arrived at migration #${migration.version}`);
    } else {
      this.logger.log("no new migration");
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
    this.logger.error(
      `migration failed. Migration #${version} is marked as dirty. Apply corrective actions and set migration version manually with manager.forceVersion(newVersion)`
    );
  }
}
