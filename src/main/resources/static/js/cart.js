//member 4
import {api, notice, failure, cell, option, money} from './api.js';
const customer = document.querySelector('#customer'), products = document.querySelector('#product'), rows = document.querySelector('#rows');
let cart = null, busy = false;
function controls() {
  customer.disabled = busy;
  document.querySelector('#load').disabled = busy || !customer.value;
  document.querySelector('#create').disabled = busy || !customer.value || !!cart;
  document.querySelector('#add').disabled = busy || !cart || products.options.length < 2;
  document.querySelector('#clear').disabled = busy || !cart || !cart.items.length;
  document.querySelector('#delete-cart').disabled = busy || !cart;
  rows.querySelectorAll('button,input').forEach(node => node.disabled = busy);
}
async function action(work) {
  if (busy) return;
  busy = true; controls();
  try { await work(); } catch (error) { failure(error); } finally { busy = false; controls(); }
}
function render() {
  rows.replaceChildren();
  document.querySelector('#total').textContent = money(cart?.totalPrice || 0);
  document.querySelector('#cart-label').textContent = cart ? 'CART / ' + cart.id.slice(0, 8).toUpperCase() : 'NO CART SELECTED';
  const empty = document.querySelector('#empty');
  empty.hidden = !!cart?.items.length;
  empty.textContent = cart ? 'Your cart is empty. Choose a product to start.' : customer.value ? 'No cart loaded. Create one for this customer.' : 'Select a customer to load or create their cart.';
  for (const item of cart?.items || []) {
    const row = document.createElement('tr'); cell(row, item.product.name); cell(row, money(item.product.price));
    const quantity = document.createElement('input'); quantity.type = 'number'; quantity.min = '1'; quantity.max = '100'; quantity.step = '1'; quantity.value = item.quantity; quantity.className = 'quantity'; quantity.required = true; quantity.setAttribute('aria-label', 'Quantity for ' + item.product.name);
    cell(row, '').append(quantity); cell(row, money(item.totalPrice));
    const actions = cell(row, ''); actions.className = 'actions';
    const update = document.createElement('button'); update.textContent = 'Update'; update.className = 'secondary';
    update.addEventListener('click', () => {
      if (!quantity.reportValidity()) return;
      action(async () => {await api('/carts/' + cart.id + '/items/' + item.product.id, 'PUT', {quantity:Number(quantity.value)}); await refresh(); notice('Quantity updated.');});
    });
    const remove = document.createElement('button'); remove.textContent = 'Remove'; remove.className = 'danger';
    remove.addEventListener('click', () => action(async () => {await api('/carts/' + cart.id + '/items/' + item.product.id, 'DELETE'); await refresh(); notice('Item removed.');}));
    actions.append(update, remove); rows.append(row);
  }
  controls();
}
async function refresh() {cart = await api('/carts/' + cart.id); render();}
async function load() {
  cart = null; render(); notice();
  if (!customer.value) return;
  localStorage.setItem('mini-store-customer', customer.value);
  try { cart = await api('/users/' + customer.value + '/cart'); render(); notice('Customer cart loaded.'); }
  catch (error) {
    if (error.status === 404 && error.message === 'This customer has no cart yet.') notice('This customer has no cart yet. Choose Create cart.');
    else throw error;
  }
}
customer.addEventListener('change', () => action(load));
document.querySelector('#load').addEventListener('click', () => action(load));
document.querySelector('#create').addEventListener('click', () => action(async () => {
  cart = await api('/carts', 'POST', {userId:Number(customer.value)}); render(); notice('Cart created.');
}));
document.querySelector('#add-form').addEventListener('submit', event => {
  event.preventDefault();
  action(async () => {await api('/carts/' + cart.id + '/items', 'POST', {productId:Number(products.value)}); await refresh(); notice('Product added.');});
});
document.querySelector('#clear').addEventListener('click', () => action(async () => {
  if (!confirm('Clear every item in this cart?')) return;
  await api('/carts/' + cart.id + '/items', 'DELETE'); await refresh(); notice('Cart cleared.');
}));
document.querySelector('#delete-cart').addEventListener('click', () => action(async () => {
  if (!confirm('Delete this cart and its items? The customer will be kept.')) return;
  await api('/carts/' + cart.id, 'DELETE'); cart = null; render(); notice('Cart deleted.');
}));
render();
action(async () => {
  const [users, catalog] = await Promise.all([api('/users'), api('/products')]);
  users.forEach(user => option(customer, user.id, user.name + ' · ' + user.email));
  catalog.forEach(product => option(products, product.id, product.name + ' · ' + money(product.price)));
  const saved = localStorage.getItem('mini-store-customer');
  if (users.some(user => String(user.id) === saved)) { customer.value = saved; await load(); }
  if (!users.length) notice('Create a customer on the Customers page first.');
  else if (!catalog.length) notice('Create a product on the Products page to add cart items.');
});
