//member 3
// Shared JSON requests and safe DOM helpers used by all four pages.
export async function api(path, method = 'GET', body) {
  const response = await fetch(path, {method, headers: body === undefined ? {} : {'Content-Type':'application/json'}, body: body === undefined ? undefined : JSON.stringify(body)});
  const data = response.status === 204 ? null : await response.json();
  if (!response.ok) {
    const error = new Error(data.message || 'Request failed.');
    error.status = response.status;
    error.fields = data.fieldErrors || {};
    throw error;
  }
  return data;
}
export function notice(message = '', error = false) {
  const target = document.querySelector('#status');
  target.textContent = message;
  target.classList.toggle('error', error);
}
export function failure(error) {
  const details = Object.entries(error.fields || {}).map(([field, message]) => `${field}: ${message}`);
  notice([error.message || 'Could not connect to the app.', ...details].join('\n'), true);
  for (const [field, message] of Object.entries(error.fields || {})) {
    const input = document.querySelector(`[name="${field}"]`);
    if (input) { input.setAttribute('aria-invalid', 'true'); input.title = message; }
  }
}
let busy = false;
export async function run(action) {
  if (busy) return;
  busy = true;
  document.querySelectorAll('[aria-invalid]').forEach(input => {input.removeAttribute('aria-invalid'); input.removeAttribute('title');});
  try { await action(); } catch (error) { failure(error); } finally { busy = false; }
}
export function cell(row, text) {
  const td = document.createElement('td'); td.textContent = text; row.append(td); return td;
}
export function button(label, action, danger = false) {
  const node = document.createElement('button'); node.type = 'button'; node.textContent = label;
  node.className = danger ? 'danger' : 'secondary'; node.addEventListener('click', () => run(action)); return node;
}
export function option(select, id, label) { select.add(new Option(label, id)); }
export const money = value => new Intl.NumberFormat('en-IN', {style:'currency', currency:'INR'}).format(value);
document.querySelectorAll('nav a').forEach(link => {if (link.pathname === location.pathname) link.setAttribute('aria-current','page');});
