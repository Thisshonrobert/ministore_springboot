//member 2
import {api, notice, run, cell, button, option, money} from './api.js';
const form = document.querySelector('#form'), rows = document.querySelector('#rows');
let editingId = null, categoryNames = new Map();
function reset() { editingId = null; form.reset(); document.querySelector('#form-title').textContent = 'Add product'; document.querySelector('#cancel').hidden = true; }
async function load() {

const categories = await api('/categories');
categoryNames = new Map(categories.map(category => [category.id, category.name]));
for (const select of [document.querySelector('#category'), document.querySelector('#filter')]) {
  const value = select.value;
  select.length = 1;
  categories.forEach(category => option(select, category.id, category.name));
  select.value = value;
}
if (!categories.length) notice('Create a category first to add products.');

  const records = await api('/products' + (document.querySelector('#filter').value ? '?categoryId=' + document.querySelector('#filter').value : ''));
  rows.replaceChildren();
  document.querySelector('#empty').hidden = records.length > 0;
  const count = document.querySelector('#count'); if (count) count.textContent = records.length + ' RECORDS';
  for (const record of records) {
    const row = document.createElement('tr');
    cell(row, record.name); cell(row, money(record.price)); cell(row, categoryNames.get(record.categoryId) || record.categoryId);
    const actions = cell(row, ''); actions.className = 'actions';
    actions.append(button('Edit', () => {
      editingId = record.id;
      for (const input of form.elements) if (input.name) input.value = record[input.name] ?? '';
      document.querySelector('#form-title').textContent = 'Edit product';
      document.querySelector('#cancel').hidden = false; form.elements.name.focus();
    }), button('Delete', async () => {
      if (!confirm(`Delete ${record.name}?`)) return;
      await api('/products/' + record.id, 'DELETE');
      if (editingId === record.id) reset();
      await load(); notice('Product deleted.');
    }, true));
    rows.append(row);
  }
}
form.addEventListener('submit', event => {
  event.preventDefault();
  run(async () => {
    const body = Object.fromEntries(new FormData(form)); body.price = Number(body.price); body.categoryId = Number(body.categoryId);
    await api('/products' + (editingId === null ? '' : '/' + editingId), editingId === null ? 'POST' : 'PUT', body);
    reset(); await load(); notice('Product saved.');
  });
});
document.querySelector('#cancel').addEventListener('click', () => {reset(); notice();});
document.querySelector('#filter').addEventListener('change', () => run(load));
run(load);
