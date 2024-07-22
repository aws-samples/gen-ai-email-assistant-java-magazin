import { JsonObject } from "./types";

export class Store {
  private static instance: Store;
  private constructor() {}

  static STORE_KEY_TOKEN = "jwt";

  public static getInstance(): Store {
    if (!Store.instance) {
      Store.instance = new Store();
    }

    return Store.instance;
  }

  set(key: string, value: JsonObject) {
    localStorage.setItem(key, JSON.stringify(value));
  }

  get(key: string) {
    const value = localStorage.getItem(key);
    return value ? JSON.parse(value) : value;
  }

  remove(key: string) {
    localStorage.removeItem(key);
  }
}
