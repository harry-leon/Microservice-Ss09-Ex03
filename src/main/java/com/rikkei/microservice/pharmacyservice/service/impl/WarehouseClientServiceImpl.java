package com.rikkei.microservice.pharmacyservice.service.impl;

import com.rikkei.microservice.pharmacyservice.dto.InventoryCheckResponse;
import com.rikkei.microservice.pharmacyservice.service.WarehouseClientService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class WarehouseClientServiceImpl implements WarehouseClientService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseClientServiceImpl.class);

    private final RestTemplate restTemplate;

    @Value("${warehouse.service.url:http://localhost:8082}")
    private String warehouseServiceUrl;

    @Override
    @CircuitBreaker(name = "warehouseCB", fallbackMethod = "warehouseFallback")
    public InventoryCheckResponse checkWarehouseStock(String medicineCode) {
        log.info("Calling warehouse-service to check stock for medicineCode: {}", medicineCode);
        String targetUrl = warehouseServiceUrl + "/api/v1/warehouse/medicines/" + medicineCode + "/stock";
        
        ResponseEntity<InventoryCheckResponse> response = restTemplate.getForEntity(targetUrl, InventoryCheckResponse.class);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }

        throw new IllegalStateException("Invalid response from warehouse-service");
    }

    /**
     * Fallback method when warehouse-service fails or Circuit Breaker is OPEN.
     */
    public InventoryCheckResponse warehouseFallback(String medicineCode, Throwable throwable) {
        log.error("Circuit Breaker Fallback executed for medicineCode: {}. Reason: {}", 
                medicineCode, throwable.getMessage());
        return InventoryCheckResponse.builder()
                .medicineCode(medicineCode)
                .medicineName("N/A")
                .availableStock(0)
                .warehouseLocation("Unknown")
                .status("FALLBACK_FAIL_FAST")
                .message("Warehouse Service is currently unavailable or circuit is OPEN: " + throwable.getClass().getSimpleName() + " - " + throwable.getMessage())
                .build();
    }
}
