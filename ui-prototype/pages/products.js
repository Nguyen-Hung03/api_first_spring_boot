let page = Number(new URLSearchParams(location.search).get('page') || 0);

function stockCell(quantity) {
    if (quantity <= 0) return '<span class="stock out">Hết hàng</span>';
    if (quantity <= 5) return `<span class="stock low">${quantity} · sắp hết</span>`;
    return `<span class="stock ok">${quantity}</span>`;
}

window.reloadPage = async function reloadPage() {
    const keyword = document.getElementById('keyword').value.trim();
    const category = document.getElementById('category').value.trim();
    const sort = document.getElementById('sort').value;
    const data = await api('GET', '/products' + query({ page, size: PAGE_SIZE, sort, keyword, category }));
    const low = data.content.filter((product) => product.stockQuantity <= 5).length;
    document.getElementById('summary').innerHTML = `
        <span class="stat"><b>${data.totalElements}</b><span>sản phẩm</span></span>
        <span class="stat"><b>${low}</b><span>sắp hết ở trang này</span></span>`;
    document.getElementById('rows').innerHTML = data.content.length
        ? data.content.map((product) => `
            <tr>
                <td>${product.id}</td>
                <td><a href="/products/${product.id}">${escapeHtml(product.name)}</a></td>
                <td><span class="chip">${escapeHtml(product.category) || '—'}</span></td>
                <td class="num">${money(product.price)}</td>
                <td class="num">${stockCell(product.stockQuantity)}</td>
                <td class="row-actions">
                    <a class="btn secondary" href="/products/${product.id}">Xem</a>
                    <a class="btn secondary" href="/products/${product.id}/edit">Sửa</a>
                    <button class="danger" type="button" data-delete-product="${product.id}">Xóa</button>
                </td>
            </tr>`).join('')
        : '<tr><td colspan="6" class="muted">Không có sản phẩm khớp bộ lọc.</td></tr>';
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
    const id = event.target.dataset.deleteProduct;
    if (!id || !confirm('Xóa sản phẩm #' + id + '?')) return;
    try {
        await api('DELETE', '/products/' + id);
        showOk('Đã xóa sản phẩm');
        await window.reloadPage();
    } catch (error) {
        showError(error);
    }
});

window.reloadPage().catch(showError);
