//member 3
import {api, notice, run, cell, button, option, money} from './api.js';
const form = document.querySelector('#form'), rows = document.querySelector('#rows');
let editingId = null, categoryNames = new Map();
function reset() { editingId = null; form.reset(); document.querySelector('#form-title').textContent = 'Add customer'; document.querySelector('#cancel').hidden = true; }
async function load() {

  const records = await api('/users');
  rows.replaceChildren();
  document.querySelector('#empty').hidden = records.length > 0;
  const count = document.querySelector('#count'); if (count) count.textContent = records.length + ' RECORDS';
  for (const record of records) {
    const row = document.createElement('tr');
    cell(row, record.id); cell(row, record.name); cell(row, record.email);
    const actions = cell(row, ''); actions.className = 'actions';
    actions.append(button('Edit', () => {
      editingId = record.id;
      for (const input of form.elements) if (input.name) input.value = record[input.name] ?? '';
      document.querySelector('#form-title').textContent = 'Edit customer';
      document.querySelector('#cancel').hidden = false; form.elements.name.focus();
    }), button('Delete', async () => {
      if (!confirm(`Delete ${record.name}?`)) return;
      await api('/users/' + record.id, 'DELETE');
      if (editingId === record.id) reset();
      await load(); notice('Customer deleted.');
    }, true));
    rows.append(row);
  }
}
form.addEventListener('submit', event => {
  event.preventDefault();
  run(async () => {
    const body = Object.fromEntries(new FormData(form));
    await api('/users' + (editingId === null ? '' : '/' + editingId), editingId === null ? 'POST' : 'PUT', body);
    reset(); await load(); notice('Customer saved.');
  });
});
document.querySelector('#cancel').addEventListener('click', () => {reset(); notice();});

run(load);
