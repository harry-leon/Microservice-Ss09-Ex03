package com.rikkei.microservice.pharmacyservice;

import com.rikkei.microservice.pharmacyservice.dto.InventoryCheckResponse;
import com.rikkei.microservice.pharmacyservice.service.WarehouseClientService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PharmacyServiceApplicationTests {

    @Autowired
    private WarehouseClientService warehouseClientService;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
        assertNotNull(warehouseClientService);
    }

    @Test
    @DisplayName("Circuit Breaker fallback executes when warehouse-service is down")
    void testFallbackWhenWarehouseDown() {
        InventoryCheckResponse response = warehouseClientService.checkWarehouseStock("MED001");
        assertNotNull(response);
        assertEquals("FALLBACK_FAIL_FAST", response.getStatus());
        assertEquals("MED001", response.getMedicineCode());
        assertTrue(response.getMessage().contains("Warehouse Service is currently unavailable"));
    }
}
