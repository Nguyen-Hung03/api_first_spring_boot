let catalog = [];
let customers = [];

function productOptions(selected) {
    return catalog.map((product) => `
        <option value="${product.id}" ${String(product.id) === String(selected) ? 'selected' : ''}>
            ${escapeHtml(product.name)} — ${money(product.price)} · kho ${product.stockQuantity}
        </option>`).join('');
}

function addLine(productId, quantity) {
    const row = document.createElement('div');
    row.className = 'line';
    row.innerHTML = `
        <select class="product" required>${productOptions(productId || catalog[0]?.id)}</select>
        <input class="qty" type="number" min="1" value="${quantity || 1}" required>
        <button class="danger remove" type="button">Xóa</button>`;
    row.querySelector('.remove').onclick = () => {
        if (document.querySelectorAll('#lines .line').length === 1) return;
        row.remove();
        updateEstimate();
    };
    row.querySelector('.product').onchange = updateEstimate;
    row.querySelector('.qty').oninput = updateEstimate;
    document.getElementById('lines').append(row);
}

function updateEstimate() {
    let total = 0;
    document.querySelectorAll('#lines .line').forEach((row) => {
        const product = catalog.find((item) => String(item.id) === row.querySelector('.product').value);
        const quantity = Number(row.querySelector('.qty').value) || 0;
        if (product) total += Number(product.price) * quantity;
    });
    document.getElementById('estimate').textContent = 'Tạm tính: ' + money(total);
}

window.reloadPage = async function reloadPage() {
    const customerPage = await api('GET', '/customers' + query({ size: 100, sort: 'fullName,asc' }));
    const productPage = await api('GET', '/products' + query({ size: 100, sort: 'name,asc' }));
    customers = customerPage.content;
    catalog = productPage.content.filter((product) => product.stockQuantity > 0);
    const select = document.getElementById('customerId');
    const previous = select.value;
    select.innerHTML = customers.map((customer) =>
        `<option value="${customer.id}">${escapeHtml(customer.fullName)} · ${escapeHtml(customer.address || 'chưa có địa chỉ')}</option>`).join('');
    if (previous) select.value = previous;
    if (!document.querySelector('#lines .line')) addLine();
    updateEstimate();
};

document.getElementById('addLine').onclick = () => {
    addLine();
    updateEstimate();
};

document.getElementById('customerId').addEventListener('change', () => {
    const customer = customers.find((item) => String(item.id) === document.getElementById('customerId').value);
    const address = document.getElementById('shippingAddress');
    if (customer && !address.value) address.placeholder = customer.address || '';
});

document.getElementById('form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const items = [...document.querySelectorAll('#lines .line')].map((row) => ({
        productId: Number(row.querySelector('.product').value),
        quantity: Number(row.querySelector('.qty').value),
    }));
    const address = document.getElementById('shippingAddress').value.trim();
    const note = document.getElementById('note').value.trim();
    try {
        const created = await api('POST', '/orders', {
            customerId: Number(document.getElementById('customerId').value),
            ...(address ? { shippingAddress: address } : {}),
            ...(note ? { note } : {}),
            items,
        });
        location.href = '/orders/' + created.id;
    } catch (error) {
        showError(error);
    }
});

window.reloadPage().catch(showError);
