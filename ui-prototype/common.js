const PAGE_SIZE = 10;

function baseUrl() {
    return localStorage.getItem('shop.baseUrl') || 'http://localhost:8088/api/v1';
}

function initials(name) {
    const parts = String(name || '?').trim().split(/\s+/);
    return (parts.length === 1 ? parts[0].slice(0, 2) : parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

function money(value) {
    return Number(value).toLocaleString('vi-VN') + ' ₫';
}

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, (ch) => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
    }[ch]));
}

function query(params) {
    const search = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== '' && value != null) search.append(key, value);
    });
    const text = search.toString();
    return text ? '?' + text : '';
}

function pathParts() {
    return location.pathname.split('/').filter(Boolean);
}

function clearError() {
    const box = document.getElementById('message');
    if (box && box.classList.contains('err')) {
        box.hidden = true;
        box.textContent = '';
    }
}

async function api(method, path, body) {
    clearError();
    let response;
    try {
        response = await fetch(baseUrl() + path, {
            method,
            headers: {
                Accept: 'application/json',
                ...(body ? { 'Content-Type': 'application/json' } : {}),
            },
            body: body ? JSON.stringify(body) : undefined,
        });
    } catch {
        const error = new Error('Không kết nối được API tại ' + baseUrl() + '. Hãy bật Spring Boot hoặc chọn Mock.');
        error.status = 0;
        throw error;
    }
    const data = response.status === 204 ? null : await response.json().catch(() => null);
    if (!response.ok) {
        const error = new Error(data?.message || response.statusText);
        error.status = response.status;
        error.code = data?.code;
        error.details = data?.details || [];
        throw error;
    }
    return data;
}

function showError(error) {
    const box = document.getElementById('message');
    const details = (error.details || []).map((item) => (item.field ? item.field + ': ' : '') + item.message).join(' · ');
    const text = [error.status || null, error.code, error.message, details].filter(Boolean).join(' — ');
    if (!box) {
        alert(text);
        return;
    }
    box.hidden = false;
    box.className = 'alert err';
    box.textContent = text;
}

function showOk(text) {
    const box = document.getElementById('message');
    box.hidden = false;
    box.className = 'alert ok';
    box.textContent = text;
}

function renderPager(container, page, onMove) {
    container.innerHTML = '';
    const prev = document.createElement('button');
    prev.className = 'secondary';
    prev.textContent = '‹ Trước';
    prev.disabled = page.page <= 0;
    prev.onclick = () => onMove(page.page - 1);
    const next = document.createElement('button');
    next.className = 'secondary';
    next.textContent = 'Sau ›';
    next.disabled = page.page + 1 >= page.totalPages;
    next.onclick = () => onMove(page.page + 1);
    const info = document.createElement('span');
    info.textContent = `Trang ${page.page + 1}/${Math.max(page.totalPages, 1)} · ${page.totalElements} bản ghi`;
    container.append(prev, info, next);
}

function formData(form, numericFields = []) {
    const data = {};
    new FormData(form).forEach((value, key) => {
        if (value === '') return;
        data[key] = numericFields.includes(key) ? Number(value) : value;
    });
    return data;
}

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('nav a').forEach((link) => {
        const href = link.getAttribute('href');
        const active = href === '/'
            ? location.pathname === '/'
            : location.pathname === href || location.pathname.startsWith(href + '/');
        if (active) link.classList.add('active');
    });

    const select = document.getElementById('baseUrl');
    if (!select) return;
    const saved = localStorage.getItem('shop.baseUrl');
    if (saved) select.value = saved;
    const paint = () => {
        const badge = document.getElementById('envBadge');
        const mock = select.value.includes('4010');
        badge.textContent = mock ? 'MOCK' : 'REAL API';
        badge.className = 'badge ' + (mock ? 'mock' : 'real');
    };
    paint();
    select.addEventListener('change', () => {
        localStorage.setItem('shop.baseUrl', select.value);
        paint();
        if (typeof window.reloadPage === 'function') {
            window.reloadPage().catch(showError);
        }
    });
});
