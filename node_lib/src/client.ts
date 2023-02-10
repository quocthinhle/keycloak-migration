import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios';

interface Migration {
  version: number;
  dirty: boolean;
}

export type ResponseInterceptor = [
  ((value: AxiosResponse) => AxiosResponse | Promise<AxiosResponse>) | null,
  (error: AxiosError) => any
];

export class Client {
  private axios: AxiosInstance;

  constructor(
    kcURL: string,
    realm: string,
    accessToken: string,
    interceptors?: ResponseInterceptor[]
  ) {
    this.axios = axios.create({
      baseURL: `${kcURL}/realms/${realm}/tera-migration`,
      headers: { Authorization: `Bearer ${accessToken}` },
    });
    if (interceptors) {
      for (let [onFulfilled, onRejected] of interceptors) {
        this.axios.interceptors.response.use(onFulfilled, onRejected);
      }
    }
  }

  public async setVersion(version: number, dirty: boolean) {
    const resp = await this.axios.post<
      Migration,
      AxiosResponse<Migration, Migration>,
      Migration
    >('', {
      version,
      dirty,
    });
    return resp.data;
  }

  public async getVersion() {
    const resp = await this.axios.get<
      Migration | null,
      AxiosResponse<Migration | null, null>,
      null
    >('');
    return {
      version: resp.data ? resp.data.version : 0,
      dirty: resp.data ? resp.data.dirty : false,
    };
  }
}
