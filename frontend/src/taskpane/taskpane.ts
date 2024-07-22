import { Store } from "./utils/store";
import { OAuthClient } from "./utils/auth";
import { getMailContent } from "./utils/mail";
import { clearLog, log } from "./utils/log";
import { toggleButton } from "./utils/ui";

// eslint-disable-next-line no-redeclare
/* eslint-disable no-undef */

/**
 * These values are injected via Webpack
 */
// @ts-ignore
const FRONTEND_URL = INJECTED_FRONTEND_URL;
// @ts-ignore
const API_URL = INJECTED_API_URL;
// @ts-ignore
const COGNITO_ENDPOINT = INJECTED_COGNITO_ENDPOINT;
// @ts-ignore
const COGNITO_CLIENT_ID = INJECTED_COGNITO_CLIENT_ID;
/* eslint-enable no-undef */

if (API_URL === undefined) {
  throw new Error("API_URL needs to be set");
}

const AUTH_REDIRECT_URI = `${FRONTEND_URL}/auth.html`;

const oAuthClient = new OAuthClient({
  clientId: COGNITO_CLIENT_ID,
  authServerUrl: COGNITO_ENDPOINT,
  redirectUri: AUTH_REDIRECT_URI,
  endpoints: {
    token: "/oauth2/token",
    authorize: "/oauth2/authorize",
  },
});

const store = Store.getInstance();

function loginWithCognito() {
  const loginStartUrl = `${COGNITO_ENDPOINT}/oauth2/authorize?client_id=${COGNITO_CLIENT_ID}&response_type=code&scope=email+openid+phone+profile&redirect_uri=${encodeURI(AUTH_REDIRECT_URI)}`;

  Office.context.ui.displayDialogAsync(loginStartUrl, { width: 50, height: 50 }, (result) => {
    const dialog = result.value;
    if (result.status === Office.AsyncResultStatus.Succeeded) {
      log(`Login gestartet`);
      dialog.addEventHandler(
        Office.EventType.DialogMessageReceived,
        async (receivedMessage: { message: string; origin: string | undefined } | { error: number }) => {
          const authCode = (<{ message: string; origin: string | undefined }>receivedMessage).message;

          dialog.close();
          await oAuthClient.handleAuthCode(authCode);
        }
      );
    } else {
      log(`Fehler: ${result.error.message}`);
      dialog.close();
    }
  });
}

async function generateReply() {
  await oAuthClient.validateTokens();
  const jwt = store.get(Store.STORE_KEY_TOKEN);
  if (!jwt) return;
  const dryRun = false;
  clearLog();

  const mail_content = await getMailContent(Office.context.mailbox.item);
  const topics = (<HTMLInputElement>document.getElementById("topics")).value;
  const json = {
    mail: mail_content,
    instructions: topics,
  };

  const options = {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${jwt.id_token}`,
    },
    body: JSON.stringify(json),
  };

  log("Rufe API auf, dies kann bis zu 30s dauern");
  toggleButton(true);
  try {
    const apiUrl = API_URL;
    const apiResponse = await fetch(apiUrl, options);
    const jsonResult = await apiResponse.json();
    const response = jsonResult.response;
    log(`Antwort mit ${response.length} Zeichen empfangen`);
    
    log("Erstelle E-Mail an Absender und CC");
    Office.context.mailbox.item.displayReplyAllForm(response);

    toggleButton();
  } catch (e) {
    toggleButton();
    log(e);
  }
}

Office.onReady(async (info) => {
  await oAuthClient.validateTokens();

  if (info.host === Office.HostType.Outlook) {
    document.getElementById("sideload-msg").style.display = "none";
    document.getElementById("app-body").style.display = "flex";
    document.getElementById("generate-reply").onclick = generateReply;
    document.getElementById("login").onclick = loginWithCognito;
    document.getElementById("logout").onclick = oAuthClient.logoutUser
    document.getElementById("mail-content").innerHTML = await getMailContent(Office.context.mailbox.item);
  }
});
