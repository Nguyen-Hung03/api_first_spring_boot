const id = pathParts()[1];

window.reloadPage = async function reloadPage() {
    const product = await api('GET', '/products/' + id);
    document.getElementById('name').textContent = product.name;
    document.getElementById('edit').href = '/products/' + id + '/edit';
    document.getElementById('info').innerHTML = `
        <dt>ID</dt><dd>${product.id}</dd>
        <dt>Danh mục</dt><dd>${escapeHtml(product.category) || '—'}</dd>
        <dt>Giá</dt><dd>${money(product.price)}</dd>
        <dt>Tồn kho</dt><dd>${product.stockQuantity}</dd>
        <dt>Mô tả</dt><dd>${escapeHtml(product.description) || '—'}</dd>`;
};

document.getElementById('remove').addEventListener('click', async () => {
    if (!confirm('Xóa sản phẩm này?')) return;
    try {
        await api('DELETE', '/products/' + id);
        location.href = '/products';
    } catch (error) {
        showError(error);
    }
});

window.reloadPage().catch(showError);
