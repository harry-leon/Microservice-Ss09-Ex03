package com.rikkei.microservice.pharmacyservice.controller;

import com.rikkei.microservice.pharmacyservice.dto.InventoryCheckResponse;
import com.rikkei.microservice.pharmacyservice.service.WarehouseClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pharmacy")
@RequiredArgsConstructor
public class PharmacyRetailController {

    private final WarehouseClientService warehouseClientService;

    /**
     * Endpoint for retail counter to check medicine inventory in central warehouse.
     * Uses Circuit Breaker 'warehouseCB'.
     */
    @GetMapping("/check-warehouse/{medicineCode}")
    public ResponseEntity<InventoryCheckResponse> checkWarehouseStock(@PathVariable("medicineCode") String medicineCode) {
        InventoryCheckResponse result = warehouseClientService.checkWarehouseStock(medicineCode);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "pharmacy-service");
        status.put("status", "UP");
        status.put("circuitBreaker", "warehouseCB");
        status.put("description", "Resilience4j Circuit Breaker for Warehouse Service");
        return ResponseEntity.ok(status);
    }
}
