# [Bài tập] Triển khai Circuit Breaker cho dịch vụ Kiểm tra kho thuốc

## 1. Mục tiêu
- **Kiến thức**: Ngắt mạch để bảo vệ hệ thống khi dịch vụ kho trung tâm bị lỗi.
- **Kỹ năng**: Cấu hình ngưỡng lỗi (`failure-rate-threshold`) và thời gian chờ (`wait-duration-in-open-state`) cho Circuit Breaker.

---

## 2. Mô tả chi tiết
- **Bối cảnh**: Khi bán lẻ, `pharmacy-service` gọi API sang `warehouse-service` (Kho tổng) để kiểm tra hàng tồn. Nếu `warehouse-service` phản hồi chậm hoặc sập, dịch vụ bán lẻ phải tự ngắt mạch để tránh treo máy tính tiền.
- **Yêu cầu kỹ thuật**:
  - Cấu hình Resilience4j trên Git/Config repo:
    ```properties
    resilience4j.circuitbreaker.instances.warehouseCB.failure-rate-threshold=50
    resilience4j.circuitbreaker.instances.warehouseCB.wait-duration-in-open-state=20s
    ```
  - Sử dụng `@CircuitBreaker(name = "warehouseCB")` cho hàm gọi kho tổng.

---

## 3. Cấu hình Resilience4j (`config-repo/pharmacy-service.properties` & `application.properties`)
```properties
# Resilience4j Circuit Breaker Configuration for Warehouse Service
resilience4j.circuitbreaker.instances.warehouseCB.sliding-window-type=COUNT_BASED
resilience4j.circuitbreaker.instances.warehouseCB.sliding-window-size=10
resilience4j.circuitbreaker.instances.warehouseCB.minimum-number-of-calls=5
resilience4j.circuitbreaker.instances.warehouseCB.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.warehouseCB.wait-duration-in-open-state=20s
resilience4j.circuitbreaker.instances.warehouseCB.permitted-number-of-calls-in-half-open-state=3
resilience4j.circuitbreaker.instances.warehouseCB.automatic-transition-from-open-to-half-open-enabled=true
resilience4j.circuitbreaker.instances.warehouseCB.record-exceptions=org.springframework.web.client.RestClientException,java.util.concurrent.TimeoutException,java.io.IOException

# Target Warehouse Service URL
warehouse.service.url=http://localhost:8082
```

---

## 4. Cấu trúc Source Code
```text
Ss09-Ex03/
├── .gitignore
├── pom.xml
├── README.md
├── config-repo/
│   └── pharmacy-service.properties
└── src/
    ├── main/
    │   ├── java/com/rikkei/microservice/pharmacyservice/
    │   │   ├── PharmacyServiceApplication.java
    │   │   ├── config/
    │   │   │   └── RestTemplateConfig.java
    │   │   ├── controller/
    │   │   │   └── PharmacyRetailController.java
    │   │   ├── dto/
    │   │   │   └── InventoryCheckResponse.java
    │   │   └── service/
    │   │       ├── WarehouseClientService.java
    │   │       └── impl/
    │   │           └── WarehouseClientServiceImpl.java
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/rikkei/microservice/pharmacyservice/
            └── PharmacyServiceApplicationTests.java
```

---

## 5. API Endpoints
- **Kiểm tra tồn kho qua Circuit Breaker**:
  - `GET http://localhost:8081/api/v1/pharmacy/check-warehouse/{medicineCode}`
- **Giám sát Circuit Breaker Actuator**:
  - `GET http://localhost:8081/actuator/health`
  - `GET http://localhost:8081/actuator/circuitbreakers`

---

## 6. Kết quả mong muốn
Khi giả lập `warehouse-service` bị sập hoặc không phản hồi:
- Lần gọi đầu khi lỗi, fallback trả lời nhanh:
```json
{
  "medicineCode": "MED001",
  "medicineName": "N/A",
  "availableStock": 0,
  "warehouseLocation": "Unknown",
  "status": "FALLBACK_FAIL_FAST",
  "message": "Warehouse Service is currently unavailable or circuit is OPEN: ..."
}
```
- Sau số lần gọi lỗi vượt quá `failure-rate-threshold=50%` (tối thiểu 5 cuộc gọi), mạch chuyển sang trạng thái `OPEN`. Các request tiếp theo được fail-fast ngay lập tức trong vòng `wait-duration-in-open-state=20s` mà không cần chờ timeout, bảo vệ hệ thống bán lẻ khỏi bị treo.
