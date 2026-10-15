const id = pathParts()[1];

window.reloadPage = async function reloadPage() {
    const customer = await api('GET', '/customers/' + id);
    const orders = await api('GET', '/customers/' + id + '/orders' + query({ size: PAGE_SIZE, sort: 'id,desc' }));
    document.getElementById('name').textContent = customer.fullName;
    document.getElementById('edit').href = '/customers/' + id + '/edit';
    document.getElementById('info').innerHTML = `
        <dt>ID</dt><dd>${customer.id}</dd>
        <dt>Email</dt><dd>${escapeHtml(customer.email)}</dd>
        <dt>Điện thoại</dt><dd>${escapeHtml(customer.phone) || '—'}</dd>
        <dt>Địa chỉ</dt><dd>${escapeHtml(customer.address) || '—'}</dd>`;
    document.getElementById('orders').innerHTML = orders.content.length
        ? orders.content.map((order) => `
            <tr>
                <td><a href="/orders/${order.id}">${order.id}</a></td>
                <td><span class="status ${escapeHtml(order.status)}">${escapeHtml(order.status)}</span></td>
                <td class="num">${money(order.totalAmount)}</td>
            </tr>`).join('')
        : '<tr><td colspan="3" class="muted">Chưa có đơn hàng.</td></tr>';
};

document.getElementById('remove').addEventListener('click', async () => {
    if (!confirm('Xóa khách hàng này?')) return;
    try {
        await api('DELETE', '/customers/' + id);
        location.href = '/customers';
    } catch (error) {
        showError(error);
    }
});

window.reloadPage().catch(showError);
