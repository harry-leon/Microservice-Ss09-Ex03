package com.rikkei.microservice.pharmacyservice.service;

import com.rikkei.microservice.pharmacyservice.dto.InventoryCheckResponse;

public interface WarehouseClientService {
    InventoryCheckResponse checkWarehouseStock(String medicineCode);
}
