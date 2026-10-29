'use strict';
const $ = (selector) => document.querySelector(selector);
const money = (value) => new Intl.NumberFormat('id-ID', { style: 'currency', currency: 'IDR', maximumFractionDigits: 2 }).format(value);
const escapeHtml = (value) => String(value).replace(/[&<>"']/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[char]));
const storageKey = 'allo-split-bill-v1';
let groups = [];
try {
  const saved = JSON.parse(localStorage.getItem(storageKey) || '[]');
  if (Array.isArray(saved)) groups = saved.filter(g => Number.isSafeInteger(g.id) && typeof g.name === 'string' && Array.isArray(g.participants) && g.participants.every(p => Number.isSafeInteger(p.id) && typeof p.name === 'string')).map(g => ({ ...g, expenses: Array.isArray(g.expenses) ? g.expenses : [] }));
} catch { /* A fresh workspace is safe when browser storage is unavailable. */ }
let activeId = groups[0]?.id;
let settlementRequest = 0;
function persist() {
  try { localStorage.setItem(storageKey, JSON.stringify(groups)); }
  catch { notice('Data berhasil disimpan di server, tetapi riwayat tidak dapat disimpan di browser ini.'); }
}
function notice(message) { $('#notice').textContent = message; $('#notice').hidden = false; }
async function api(path, body) {
  const response = await fetch(`/api/bill-groups${path}`, { method: body ? 'POST' : 'GET', headers: { Accept: 'application/json', ...(body ? { 'Content-Type': 'application/json' } : {}) }, ...(body ? { body: JSON.stringify(body) } : {}), signal: AbortSignal.timeout(20000) });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(data.errors?.map(e => `${e.field || ''} ${e.message || ''}`).join(', ') || data.detail || `Permintaan gagal (${response.status}).`);
  return data;
}
function errorText(error) { return error.name === 'TimeoutError' ? 'Server terlalu lama merespons. Coba lagi sebentar.' : error instanceof TypeError ? 'Tidak dapat terhubung ke server. Periksa koneksi dan coba lagi.' : error.message; }
function activeGroup() { return groups.find(g => g.id === activeId); }
function showExpenses() {
  settlementRequest++;
  $('#expenses-tab').classList.add('active'); $('#settlement-tab').classList.remove('active');
  $('#expenses-panel').hidden = false; $('#settlement-panel').hidden = true;
}
function render() {
  $('#group-count').textContent = `${groups.length} grup`;
  $('#empty').hidden = groups.length > 0;
  $('#group-detail').hidden = !activeGroup();
  $('#groups').innerHTML = groups.map(g => `<button class="group-card ${g.id === activeId ? 'active' : ''}" data-group="${g.id}" aria-pressed="${g.id === activeId}"><b>${escapeHtml(g.name)}</b><small>${g.participants.length} anggota · ${g.expenses.length} pengeluaran</small></button>`).join('');
  const group = activeGroup();
  if (!group) return;
  $('#group-title').textContent = group.name;
  $('#members').textContent = group.participants.map(p => p.name).join(' · ');
  $('#expenses-panel').innerHTML = group.expenses.length ? group.expenses.map(e => `<div class="expense-row"><div><b>${escapeHtml(e.description)}</b><small>Dibayar ${escapeHtml(e.paidBy.name)} · dibagi ${e.participants.length} orang</small></div><strong class="amount">${money(e.amount)}</strong></div>`).join('') : '<p class="muted">Belum ada pengeluaran. Tambahkan tagihan pertama untuk mulai patungan.</p>';
  showExpenses();
}
function openGroup() { $('#group-form').reset(); $('#group-form .form-error').textContent = ''; $('#group-dialog').showModal(); }
['#new-group', '#empty-create'].forEach(s => $(s).addEventListener('click', openGroup));
document.querySelectorAll('[data-close]').forEach(button => button.addEventListener('click', () => button.closest('dialog').close()));
$('#groups').addEventListener('click', (event) => { const button = event.target.closest('[data-group]'); if (button) { activeId = Number(button.dataset.group); render(); } });
async function submitForm(form, action) {
  const button = form.querySelector('[type=submit]');
  const label = button.textContent;
  button.disabled = true; button.textContent = 'Menyimpan…'; form.querySelector('.form-error').textContent = '';
  try { await action(); } catch (error) { form.querySelector('.form-error').textContent = errorText(error); }
  finally { button.disabled = false; button.textContent = label; }
}
$('#group-form').addEventListener('submit', (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  submitForm(form, async () => {
    const data = new FormData(form);
    const name = data.get('name').trim();
    const participants = data.get('participants').split('\n').map(s => s.trim()).filter(Boolean).map(name => ({ name }));
    if (!name || !participants.length) throw new Error('Isi nama grup dan setidaknya satu anggota.');
    if (participants.some(p => p.name.length > 100)) throw new Error('Nama anggota maksimal 100 karakter.');
    const group = await api('', { name, participants });
    groups.unshift({ ...group, expenses: [] }); activeId = group.id; persist(); render(); $('#group-dialog').close();
  });
});
$('#add-expense').addEventListener('click', () => {
  const group = activeGroup(); if (!group) return;
  $('#expense-form').reset(); $('#expense-form').dataset.groupId = group.id; $('#expense-form .form-error').textContent = '';
  $('#expense-form select').innerHTML = group.participants.map(p => `<option value="${p.id}">${escapeHtml(p.name)} (#${p.id})</option>`).join('');
  $('#beneficiaries').innerHTML = group.participants.map(p => `<label><input type="checkbox" name="participantIds" value="${p.id}" checked>${escapeHtml(p.name)} <small>#${p.id}</small></label>`).join('');
  $('#expense-dialog').showModal();
});
$('#expense-form').addEventListener('submit', event => {
  event.preventDefault(); const form = event.currentTarget;
  submitForm(form, async () => {
    const data = new FormData(form); const description = data.get('description').trim();
    const participantIds = data.getAll('participantIds').map(Number);
    if (!description) throw new Error('Isi deskripsi pengeluaran.');
    if (!participantIds.length) throw new Error('Pilih setidaknya satu anggota untuk pembagian.');
    const groupId = Number(form.dataset.groupId);
    const expense = await api(`/${groupId}/expenses`, { description, amount: data.get('amount'), paidBy: Number(data.get('paidBy')), participantIds });
    groups.find(g => g.id === groupId).expenses.push(expense); persist(); render(); $('#expense-dialog').close();
  });
});
$('#expenses-tab').addEventListener('click', showExpenses);
async function loadSettlement() {
  const group = activeGroup(); if (!group) return;
  const request = ++settlementRequest;
  $('#expenses-tab').classList.remove('active'); $('#settlement-tab').classList.add('active');
  $('#expenses-panel').hidden = true; $('#settlement-panel').hidden = false;
  $('#settlement-panel').innerHTML = '<p class="muted" role="status">Menghitung pembagian…</p>';
  try {
    const result = await api(`/${group.id}/settlement`);
    if (request !== settlementRequest) return;
    const transactions = result.transactions;
    $('#settlement-panel').innerHTML = `<div class="summary"><p><span>Total pengeluaran</span><span>${money(result.totalExpense)}</span></p><p><span>Biaya layanan (${escapeHtml(result.service_charge_pct)}%)</span><span>${money(result.service_charge_amount)}</span></p><p><span>Total termasuk biaya</span><span>${money(Number(result.totalExpense) + Number(result.service_charge_amount))}</span></p></div><p class="muted">${transactions.length ? `${transactions.length} transfer yang perlu dilakukan. Biaya layanan sudah termasuk.` : 'Tidak ada transfer yang diperlukan.'}</p>${transactions.map(t => `<div class="transfer"><div><b>${escapeHtml(t.from)} → ${escapeHtml(t.to)}</b><small>Anggota #${t.fromId} ke #${t.toId}</small></div><strong class="amount">${money(t.amount)}</strong></div>`).join('')}<p class="muted">Settlement adalah perhitungan pembagian, bukan status pembayaran.</p><button class="secondary refresh" id="refresh-settlement">↻ Perbarui perhitungan</button>`;
  } catch (error) {
    if (request !== settlementRequest) return;
    $('#settlement-panel').innerHTML = `<p class="form-error" role="alert">${escapeHtml(errorText(error))}</p><button class="secondary" id="refresh-settlement">Coba lagi</button>`;
  }
}
$('#settlement-tab').addEventListener('click', loadSettlement);
$('#settlement-panel').addEventListener('click', event => { if (event.target.closest('#refresh-settlement')) loadSettlement(); });
render();
