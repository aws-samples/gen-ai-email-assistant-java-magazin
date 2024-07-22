import { getMailContent } from "./mail";

export async function adaptUiToLoggedInUser(userId: string) {
  document.getElementById("mail-content").innerHTML = await getMailContent(Office.context.mailbox.item);
  $("#generate-reply").show();
  $("#logged-in-buttons").show();
  $("#logged-in-container").show();
  $("#logged-out-container").hide();
  $("#login").hide();
  $("#logout").show();
}

export async function adaptUiToLoggedOutUser() {
  $("#generate-reply").hide();
  $("#logged-in-buttons").hide();
  $("#logged-in-container").hide();
  $("#logged-out-container").show();
  $("#login").show();
  $("#logout").hide();
}

export function toggleButton(spinning = false) {
  if (spinning) {
    $("#generate-reply-spinner").show();
    $("#generate-reply").hide();
  } else {
    $("#generate-reply-spinner").hide();
    $("#generate-reply").show();
  }
}
