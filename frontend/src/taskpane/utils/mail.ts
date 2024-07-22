import MessageRead = Office.MessageRead;

export function getMailContent(item: Office.MessageRead = Office.context.mailbox.item, asHtml = true): Promise<string> {
  return new Promise((resolve, reject) => {
    const coercionType = asHtml ? Office.CoercionType.Html : Office.CoercionType.Text;
    item.body.getAsync(coercionType, function (asyncResult) {
      if (asyncResult.status !== Office.AsyncResultStatus.Succeeded) {
        reject(asyncResult.status);
      } else {
        resolve(asyncResult.value);
      }
    });
  });
}
