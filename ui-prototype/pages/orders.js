let page = Number(new URLSearchParams(location.search).get('page') || 0);

window.reloadPage = async function reloadPage() {
    const status = document.getElementById('status').value;
    const data = await api('GET', '/orders' + query({ page, size: PAGE_SIZE, sort: 'id,desc', status }));
    document.getElementById('rows').innerHTML = data.content.length
        ? data.content.map((order) => `
            <tr>
                <td><a href="/orders/${order.id}">${order.id}</a></td>
                <td><a href="/customers/${order.customerId}">${escapeHtml(order.customerName || order.customerId)}</a></td>
                <td>${order.items.map((item) => escapeHtml(item.productName) + ' × ' + item.quantity).join('<br>')}</td>
                <td class="num">${money(order.totalAmount)}</td>
                <td><span class="status ${escapeHtml(order.status)}">${escapeHtml(order.status)}</span></td>
                <td class="row-actions"><a class="btn secondary" href="/orders/${order.id}">Xem</a></td>
            </tr>`).join('')
        : '<tr><td colspan="6" class="muted">Không có đơn hàng khớp bộ lọc.</td></tr>';
    document.getElementById('summary').innerHTML = `<span class="stat"><b>${data.totalElements}</b><span>đơn hàng</span></span>`;
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

window.reloadPage().catch(showError);
