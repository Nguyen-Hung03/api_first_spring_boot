const parts = pathParts();
const editing = parts[2] === 'edit';
const id = editing ? parts[1] : null;

window.reloadPage = async function reloadPage() {
    if (!editing) return;
    const customer = await api('GET', '/customers/' + id);
    document.getElementById('title').textContent = 'Sửa khách hàng';
    document.getElementById('submit').textContent = 'Lưu';
    document.getElementById('fullName').value = customer.fullName || '';
    document.getElementById('email').value = customer.email || '';
    document.getElementById('phone').value = customer.phone || '';
    document.getElementById('address').value = customer.address || '';
};

document.getElementById('form').addEventListener('submit', async (event) => {
    event.preventDefault();
    const body = formData(event.target);
    try {
        if (editing) {
            await api('PUT', '/customers/' + id, body);
            location.href = '/customers/' + id;
        } else {
            const created = await api('POST', '/customers', body);
            location.href = '/customers/' + created.id;
        }
    } catch (error) {
        showError(error);
    }
});

window.reloadPage().catch(showError);
