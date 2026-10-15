let page = Number(new URLSearchParams(location.search).get('page') || 0);

function summary(data) {
    document.getElementById('summary').innerHTML = `<span class="stat"><b>${data.totalElements}</b><span>khách hàng</span></span>`;
}

window.reloadPage = async function reloadPage() {
    const keyword = document.getElementById('keyword').value.trim();
    const sort = document.getElementById('sort').value;
    const data = await api('GET', '/customers' + query({ page, size: PAGE_SIZE, sort, keyword }));
    summary(data);
    document.getElementById('rows').innerHTML = data.content.length
        ? data.content.map((customer) => `
            <tr>
                <td>${customer.id}</td>
                <td><a class="person" href="/customers/${customer.id}"><span class="avatar">${escapeHtml(initials(customer.fullName))}</span>${escapeHtml(customer.fullName)}</a></td>
                <td>${escapeHtml(customer.email)}</td>
                <td>${escapeHtml(customer.phone)}</td>
                <td>${escapeHtml(customer.address)}</td>
                <td class="row-actions">
                    <a class="btn secondary" href="/customers/${customer.id}">Xem</a>
                    <a class="btn secondary" href="/customers/${customer.id}/edit">Sửa</a>
                    <button class="danger" type="button" data-delete-customer="${customer.id}">Xóa</button>
                </td>
            </tr>`).join('')
        : '<tr><td colspan="6" class="muted">Không có khách hàng khớp bộ lọc.</td></tr>';
    renderPager(document.getElementById('pager'), data, (next) => {
        page = next;
        reloadPage().catch(showError);
    });
};

document.getElementById('filter').addEventListener('submit', (event) => {
    event.preventDefault();
    page = 0;
    window.reloadPage().catch(showError);
});

document.getElementById('rows').addEventListener('click', async (event) => {
    const id = event.target.dataset.deleteCustomer;
    if (!id || !confirm('Xóa khách hàng #' + id + '?')) return;
    try {
        await api('DELETE', '/customers/' + id);
        showOk('Đã xóa khách hàng');
        await window.reloadPage();
    } catch (error) {
        showError(error);
    }
});

window.reloadPage().catch(showError);
