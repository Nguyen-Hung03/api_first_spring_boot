const parts = pathParts();
const editing = parts[2] === 'edit';
const id = editing ? parts[1] : null;

window.reloadPage = async function reloadPage() {
    if (!editing) return;
    const product = await api('GET', '/products/' + id);
    document.getElementById('title').textContent = 'Sửa sản phẩm';
    document.getElementById('submit').textContent = 'Lưu';
    document.getElementById('name').value = product.name || '';
    document.getElementById('category').value = product.category || '';
    document.getElementById('price').value = product.price ?? '';
    document.getElementById('stockQuantity').value = product.stockQuantity ?? '';
    document.getElementById('description').value = product.description || '';
};

document.getElementById('form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const body = formData(event.target, ['price', 'stockQuantity']);
    try {
        if (editing) {
            await api('PUT', '/products/' + id, body);
            location.href = '/products/' + id;
        } else {
            const created = await api('POST', '/products', body);
            location.href = '/products/' + created.id;
        }
    } catch (error) {
        showError(error);
    }
});

window.reloadPage().catch(showError);
