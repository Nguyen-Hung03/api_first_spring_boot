const http = require('node:http');
const fs = require('node:fs');
const path = require('node:path');

const root = __dirname;
const port = Number(process.env.PORT) || 5500;

const pages = [
    [/^\/$/, 'index.html'],
    [/^\/customers$/, 'customers.html'],
    [/^\/customers\/new$/, 'customer-form.html'],
    [/^\/customers\/\d+$/, 'customer.html'],
    [/^\/customers\/\d+\/edit$/, 'customer-form.html'],
    [/^\/products$/, 'products.html'],
    [/^\/products\/new$/, 'product-form.html'],
    [/^\/products\/\d+$/, 'product.html'],
    [/^\/products\/\d+\/edit$/, 'product-form.html'],
    [/^\/orders$/, 'orders.html'],
    [/^\/orders\/new$/, 'order-form.html'],
    [/^\/orders\/\d+$/, 'order.html'],
];

const types = {
    '.html': 'text/html; charset=utf-8',
    '.css': 'text/css; charset=utf-8',
    '.js': 'text/javascript; charset=utf-8',
};

function send(res, status, type, body) {
    res.writeHead(status, { 'Content-Type': type, 'Cache-Control': 'no-store' });
    res.end(body);
}

const server = http.createServer((req, res) => {
    const url = new URL(req.url, 'http://localhost');
    const page = pages.find(([pattern]) => pattern.test(url.pathname));
    let relative = page ? page[1] : null;

    if (!relative && /^\/(styles\.css|common\.js|pages\/[a-z-]+\.js)$/.test(url.pathname)) {
        relative = url.pathname.slice(1);
    }
    if (!relative) {
        send(res, 404, types['.html'], '<p>Không có trang này. <a href="/">Về trang chủ</a></p>');
        return;
    }

    const full = path.resolve(root, relative);
    if (!full.startsWith(root)) {
        send(res, 403, 'text/plain', 'Forbidden');
        return;
    }
    fs.readFile(full, (err, data) => {
        if (err) {
            send(res, 404, types['.html'], '<p>Không tìm thấy trang.</p>');
            return;
        }
        send(res, 200, types[path.extname(full)] || 'application/octet-stream', data);
    });
});

server.listen(port, () => {
    console.log(`View (Node): http://localhost:${port}`);
});
