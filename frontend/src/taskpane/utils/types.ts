export type JsonObject = {
  [key: string]: string | number | boolean | any[];
};

export interface OAuthResponse extends JsonObject {
  id_token: string;
  access_token: string;
  refresh_token: string;
  token_type: "Bearer";
  expires_in: number;
}
