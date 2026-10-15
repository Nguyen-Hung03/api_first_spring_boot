const NEXT_STATUSES = {
    PENDING: ['CONFIRMED', 'CANCELLED'],
    CONFIRMED: ['SHIPPING', 'CANCELLED'],
    SHIPPING: ['DELIVERED'],
    DELIVERED: [],
    CANCELLED: [],
};

const id = pathParts()[1];

window.reloadPage = async function reloadPage() {
    const order = await api('GET', '/orders/' + id);
    document.getElementById('title').textContent = 'Đơn hàng #' + order.id;
    document.getElementById('info').innerHTML = `
        <dt>Khách hàng</dt><dd><a href="/customers/${order.customerId}">${escapeHtml(order.customerName || order.customerId)}</a></dd>
        <dt>Trạng thái</dt><dd><span class="status ${escapeHtml(order.status)}">${escapeHtml(order.status)}</span></dd>
        <dt>Địa chỉ giao</dt><dd>${escapeHtml(order.shippingAddress) || '—'}</dd>
        <dt>Ghi chú</dt><dd>${escapeHtml(order.note) || '—'}</dd>
        <dt>Tổng tiền</dt><dd>${money(order.totalAmount)}</dd>`;
    document.getElementById('items').innerHTML = order.items.map((item) => `
        <tr>
            <td><a href="/products/${item.productId}">${escapeHtml(item.productName)}</a></td>
            <td class="num">${money(item.unitPrice)}</td>
            <td class="num">${item.quantity}</td>
            <td class="num">${money(item.lineTotal)}</td>
        </tr>`).join('');
    const actions = document.getElementById('statusActions');
    actions.innerHTML = '';
    (NEXT_STATUSES[order.status] || []).forEach((status) => {
        const button = document.createElement('button');
        button.className = 'secondary';
        button.textContent = 'Chuyển sang ' + status;
        button.onclick = async () => {
            try {
                await api('PATCH', '/orders/' + id, { status });
                showOk('Đã chuyển sang ' + status);
                await reloadPage();
            } catch (error) {
                showError(error);
            }
        };
        actions.append(button);
    });
};

document.getElementById('remove').addEventListener('click', async () => {
    if (!confirm('Xóa đơn hàng này?')) return;
    try {
        await api('DELETE', '/orders/' + id);
        location.href = '/orders';
    } catch (error) {
        showError(error);
    }
});

window.reloadPage().catch(showError);
