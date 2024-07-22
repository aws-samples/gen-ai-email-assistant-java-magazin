import { JsonObject, OAuthResponse } from "./types";
import { Store } from "./store";
import { adaptUiToLoggedInUser, adaptUiToLoggedOutUser } from "./ui";
import { log } from "./log";

interface OAuthConfig {
  clientId: string;
  redirectUri: string;
  authServerUrl: string;
  endpoints: {
    token: string;
    authorize: string;
  };
}

export class OAuthClient {
  private readonly config: OAuthConfig;
  constructor(config: OAuthConfig) {
    this.config = config;
  }

  async handleAuthCode(authCode: string) {
    try {
      const requestBody = {
        grant_type: "authorization_code",
        client_id: this.config.clientId,
        redirect_uri: this.config.redirectUri,
        code: authCode,
      };

      const formBody = Object.keys(requestBody)
        .map((key) => encodeURIComponent(key) + "=" + encodeURIComponent(requestBody[key]))
        .join("&");

      const apiResponse = await fetch(this.config.authServerUrl + this.config.endpoints.token, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded", "User-Agent": "insomnia/2023.5.8" },
        body: formBody,
      });
      const jwt = await apiResponse.json();
      await this.loginUser(jwt);
      log(`Verwende ID Token: ${jwt.id_token.substring(0, 10)}..`);
    } catch (e) {
      log(e);
      await this.logoutUser();
      log("Authorization Code konnte nicht gegen JWT eingetauscht werden");
    }
  }

  async refreshTokens(refreshToken: string) {
    try {
      const requestBody = {
        grant_type: "refresh_token",
        client_id: this.config.clientId,
        redirect_uri: this.config.redirectUri,
        refresh_token: refreshToken,
      };

      const formBody = Object.keys(requestBody)
        .map((key) => encodeURIComponent(key) + "=" + encodeURIComponent(requestBody[key]))
        .join("&");

      const apiResponse = await fetch(this.config.authServerUrl + this.config.endpoints.token, {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded", "User-Agent": "insomnia/2023.5.8" },
        body: formBody,
      });
      const json = await apiResponse.json();

      if (apiResponse.status !== 200 || !("id_token" in json)) {
        log("Unable to refresh tokens - user needs to sign in again");
        return undefined;
      } else {
        return json;
      }
    } catch (e) {
      log("Unable to refresh tokens - user needs to sign in again");
      return undefined;
    }
  }

  async loginUser(jwt: OAuthResponse) {
    Store.getInstance().set(Store.STORE_KEY_TOKEN, jwt);

    const parsedToken = parseJwt(jwt.id_token);
    const userId = parsedToken.email as string;
    log(`Eingeloggt als Nutzer: ${userId}`);
    await adaptUiToLoggedInUser(userId);
  }

  async logoutUser() {
    Store.getInstance().remove(Store.STORE_KEY_TOKEN);
    await adaptUiToLoggedOutUser();
  }

  async validateTokens() {
    log("Validiere Token");
    const jwt = Store.getInstance().get(Store.STORE_KEY_TOKEN);
    if (!jwt) {
      log("Kein Token gefunden, Login nötig");
      return;
    }

    log("Gespeichertes Token gefunden, prüfe Gültigkeit");
    const now = Math.round(Date.now() / 1000);
    const tokenExpiration = parseJwt(jwt.id_token).exp;

    // check if the id/access token is still valid
    if (tokenExpiration < now) {
      log("Gespeichertes Token ist abgelaufen, versuche es zu erneuern");
      const newTokens = await this.refreshTokens(jwt.refresh_token);
      if (newTokens === undefined) {
        await this.logoutUser();
      } else {
        log("Token erneuert");
        jwt.id_token = newTokens.id_token;
        jwt.access_token = newTokens.access_token;
        await this.loginUser(jwt);
      }
    } else {
      log("Gespeichertes Token ist gültig");
      await this.loginUser(jwt);
    }
  }
}

export function parseJwt(token: string): JsonObject {
  const base64Url = token.split(".")[1];
  const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
  const jsonPayload = decodeURIComponent(
    window
      .atob(base64)
      .split("")
      .map(function (c) {
        return "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2);
      })
      .join("")
  );

  return JSON.parse(jsonPayload);
}
