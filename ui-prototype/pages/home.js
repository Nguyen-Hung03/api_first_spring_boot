window.reloadPage = async function reloadPage() {
    const [customers, products, orders] = await Promise.all([
        api('GET', '/customers' + query({ size: 1 })),
        api('GET', '/products' + query({ size: 100, sort: 'name,asc' })),
        api('GET', '/orders' + query({ size: 100, sort: 'id,desc' })),
    ]);
    const lowStock = products.content.filter((product) => product.stockQuantity <= 5).length;
    const pending = orders.content.filter((order) => order.status === 'PENDING' || order.status === 'CONFIRMED' || order.status === 'SHIPPING').length;
    document.getElementById('stats').innerHTML = `
        <a class="stat" href="/customers"><b>${customers.totalElements}</b><span>Khách hàng</span></a>
        <a class="stat" href="/products"><b>${products.totalElements}</b><span>Sản phẩm</span></a>
        <a class="stat" href="/products"><b>${lowStock}</b><span>Sắp hết hoặc hết hàng</span></a>
        <a class="stat" href="/orders"><b>${orders.totalElements}</b><span>Đơn hàng</span></a>
        <a class="stat" href="/orders"><b>${pending}</b><span>Đơn đang xử lý</span></a>`;
};

window.reloadPage().catch(showError);
