# Đặc tả mô tả chức năng

**Hệ thống:** Shop API  
**Contract:** `src/main/resources/shop-api.yaml` (OpenAPI 3.0.3)  
**Base URL:** `http://localhost:8088/api/v1`  
**Phiên bản:** 1.0 · 15/10/2026

Tài liệu này mô tả từng chức năng nhân viên dùng được. Chi tiết trường dữ liệu và ví dụ JSON nằm trong file contract. Trang xem trên trình duyệt (`http://localhost:5500`) chỉ gọi các chức năng này, không thêm quy tắc riêng.

## 1. Quy ước chung

| Việc | Cách làm |
|---|---|
| Danh sách | `GET` tập hợp, query `page` (từ 0), `size` (1–100, mặc định 10), `sort` (`trường,asc` hoặc `trường,desc`) |
| Tạo | `POST`, trả **201** và header `Location` trỏ tới bản ghi mới |
| Sửa toàn bộ | `PUT`, các trường bắt buộc phải có |
| Sửa một phần | `PATCH`, chỉ gửi trường cần đổi |
| Xóa | `DELETE`, thành công trả **204** |
| Lỗi nhập liệu | **400**, `code = VALIDATION_FAILED` hoặc `INVALID_SORT_FIELD` |
| Không thấy | **404**, `code = RESOURCE_NOT_FOUND` |
| Sai nghiệp vụ | **422**, `code` nêu ở từng chức năng |

Mọi lỗi có dạng:

```json
{
  "status": 422,
  "code": "INSUFFICIENT_STOCK",
  "message": "Không đủ hàng tồn kho để đặt hàng",
  "details": [{ "field": "items.quantity", "message": "..." }],
  "path": "/api/v1/orders",
  "timestamp": "2026-10-15T00:00:00Z"
}
```

## 2. Khách hàng

### FN-KH-01. Xem danh sách

- **API:** `GET /customers`
- **Lọc:** `keyword` tìm trong họ tên hoặc email, không phân biệt hoa thường.
- **Sắp xếp được:** `id`, `fullName`, `email`, `createdAt`.
- **Kết quả:** trang gồm `content`, `page`, `size`, `totalElements`, `totalPages`.
- **Màn hình:** `/customers` — ô tìm, chọn kiểu sắp xếp, phân trang, nút Xem / Sửa / Xóa.

### FN-KH-02. Xem chi tiết

- **API:** `GET /customers/{customerId}`
- **Kết quả:** id, họ tên, email, điện thoại, địa chỉ, thời điểm tạo và cập nhật.
- **Ngoại lệ:** id không có → 404.
- **Màn hình:** `/customers/{id}`, kèm danh sách đơn của khách (FN-KH-06).

### FN-KH-03. Thêm khách

- **API:** `POST /customers`
- **Bắt buộc:** `fullName` (2–100 ký tự), `email` (đúng định dạng, tối đa 150).
- **Tùy chọn:** `phone` (9–15 chữ số, có thể có `+`), `address` (tối đa 255).
- **Nghiệp vụ:** email đã tồn tại → 422 `EMAIL_ALREADY_EXISTS`.
- **Thành công:** 201, body là khách vừa tạo, `Location: /api/v1/customers/{id}`.
- **Màn hình:** `/customers/new`.

### FN-KH-04. Sửa toàn bộ

- **API:** `PUT /customers/{customerId}`
- **Dữ liệu:** cùng bộ trường với thêm mới. Trường không gửi sẽ bị xóa (điện thoại, địa chỉ về trống).
- **Nghiệp vụ:** email trùng khách khác → 422 `EMAIL_ALREADY_EXISTS`. Không thấy id → 404.
- **Màn hình:** `/customers/{id}/edit`.

### FN-KH-05. Sửa một phần

- **API:** `PATCH /customers/{customerId}`
- **Dữ liệu:** chỉ các trường cần đổi. Trường không gửi được giữ nguyên.
- **Màn hình:** API có; trang web dùng PUT ở màn sửa.

### FN-KH-06. Đơn của một khách

- **API:** `GET /customers/{customerId}/orders`
- **Phân trang / sắp xếp:** như danh sách đơn (`id`, `totalAmount`, `status`, `createdAt`).
- **Ngoại lệ:** không có khách → 404.
- **Màn hình:** khối “Đơn hàng” ở trang chi tiết khách. Bấm mã đơn để mở FN-DH-02.

### FN-KH-07. Xóa khách

- **API:** `DELETE /customers/{customerId}`
- **Nghiệp vụ:** khách đã có đơn → 422 `CUSTOMER_HAS_ORDERS`. Không thấy → 404.
- **Thành công:** 204.
- **Màn hình:** nút Xóa ở danh sách và trang chi tiết, có hộp xác nhận.

## 3. Sản phẩm

### FN-SP-01. Xem danh sách

- **API:** `GET /products`
- **Lọc:** `keyword` theo tên, `category` theo đúng danh mục.
- **Sắp xếp được:** `id`, `name`, `price`, `stockQuantity`, `category`, `createdAt`.
- **Màn hình:** `/products`. Tồn kho tô đậm khi còn từ 1 đến 5 (“sắp hết”) hoặc 0 (“hết hàng”).

### FN-SP-02. Xem chi tiết

- **API:** `GET /products/{productId}`
- **Kết quả:** id, tên, mô tả, danh mục, giá, tồn kho, thời điểm tạo và cập nhật.
- **Màn hình:** `/products/{id}`.

### FN-SP-03. Thêm sản phẩm

- **API:** `POST /products`
- **Bắt buộc:** `name` (2–150), `price` (≥ 0), `stockQuantity` (≥ 0).
- **Tùy chọn:** `description` (tối đa 1000), `category` (tối đa 100).
- **Thành công:** 201 và `Location`.
- **Màn hình:** `/products/new`.

### FN-SP-04. Sửa toàn bộ / một phần

- **API:** `PUT /products/{productId}` và `PATCH /products/{productId}`.
- **PUT** gửi đủ trường bắt buộc. **PATCH** chỉ gửi trường cần đổi (ví dụ chỉ đổi giá hoặc tồn kho).
- **Màn hình:** `/products/{id}/edit` dùng PUT.

### FN-SP-05. Xóa sản phẩm

- **API:** `DELETE /products/{productId}`
- **Nghiệp vụ:** đã có trong đơn → 422 `PRODUCT_IN_USE`.
- **Thành công:** 204.
- **Màn hình:** nút Xóa ở danh sách và trang chi tiết.

## 4. Đơn hàng

### FN-DH-01. Đặt hàng

- **API:** `POST /orders`
- **Bắt buộc:** `customerId`, `items` (1–50 dòng). Mỗi dòng có `productId` và `quantity` (1–1000).
- **Tùy chọn:** `shippingAddress` (tối đa 255), `note` (tối đa 500).
- **Xử lý:**
  1. Không có khách hoặc sản phẩm → 404.
  2. Cùng một sản phẩm trên nhiều dòng thì cộng số lượng.
  3. Cộng xong vẫn lớn hơn tồn kho → 422 `INSUFFICIENT_STOCK`, không trừ kho, không tạo đơn.
  4. Trừ kho, chụp tên và đơn giá hiện tại vào dòng hàng, tính `lineTotal` và `totalAmount`.
  5. Không nhập địa chỉ giao thì dùng địa chỉ khách. Trạng thái ban đầu là `PENDING`.
- **Thành công:** 201 và `Location: /api/v1/orders/{id}`.
- **Màn hình:** `/orders/new`. Thêm nhiều dòng, thấy tạm tính, chọn khách từ danh sách.

### FN-DH-02. Xem chi tiết

- **API:** `GET /orders/{orderId}`
- **Kết quả:** khách, trạng thái, địa chỉ, ghi chú, tổng tiền, từng dòng (tên, đơn giá, số lượng, thành tiền).
- **Màn hình:** `/orders/{id}`. Có nút chuyển trạng thái đúng bước kế tiếp và nút xóa nếu được phép.

### FN-DH-03. Xem danh sách

- **API:** `GET /orders`
- **Lọc:** `status` = `PENDING | CONFIRMED | SHIPPING | DELIVERED | CANCELLED`.
- **Sắp xếp được:** `id`, `totalAmount`, `status`, `createdAt`.
- **Màn hình:** `/orders`.

### FN-DH-04. Đổi trạng thái

- **API:** `PATCH /orders/{orderId}` với body `{ "status": "..." }`.
- **Luồng hợp lệ:**

| Đang ở | Được chuyển sang |
|---|---|
| PENDING | CONFIRMED, CANCELLED |
| CONFIRMED | SHIPPING, CANCELLED |
| SHIPPING | DELIVERED |
| DELIVERED, CANCELLED | không chuyển nữa |

- Gửi đúng trạng thái hiện tại thì giữ nguyên và trả 200.
- Sai bước → 422 `INVALID_STATUS_TRANSITION`.
- Chuyển sang `CANCELLED` thì cộng lại tồn kho theo số lượng đã đặt.
- **Màn hình:** các nút “Chuyển sang …” trên trang chi tiết đơn.

### FN-DH-05. Xóa đơn

- **API:** `DELETE /orders/{orderId}`
- **Được xóa:** `PENDING` (hoàn kho rồi xóa) và `CANCELLED` (kho đã hoàn lúc hủy).
- **Không xóa:** `CONFIRMED`, `SHIPPING`, `DELIVERED` → 422 `ORDER_NOT_DELETABLE`.
- **Thành công:** 204.

## 5. Chức năng hỗ trợ trên giao diện

Các mục dưới không phải API mới. Chúng gọi các chức năng ở mục 2–4.

| Màn hình | Chức năng |
|---|---|
| `/` | Đếm khách, sản phẩm, đơn, số mặt hàng sắp hết hoặc hết, số đơn đang xử lý |
| Mọi trang | Chọn nguồn dữ liệu: backend `http://localhost:8088/api/v1` hoặc Mock `http://127.0.0.1:4010` |
| Danh sách | Phân trang, thông báo lỗi đúng `code` và `message` từ API |

## 6. Dữ liệu mẫu

Lần chạy backend với database trống (hoặc còn bộ dữ liệu cũ rất ít) tự thêm 12 khách, 15 sản phẩm và các đơn ở đủ năm trạng thái, để danh sách, phân trang, hết hàng và hủy đơn có dữ liệu để xem.
