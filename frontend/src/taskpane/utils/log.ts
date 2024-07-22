export function clearLog() {
  document.getElementById("debug").innerHTML = "";
}

export function log(message: string) {
  console.log(message);
  const log = `<b>${new Date().toISOString()}</b>: ${message}`;
  document.getElementById("debug").innerHTML = document.getElementById("debug").innerHTML + "<br>" + log;
}
