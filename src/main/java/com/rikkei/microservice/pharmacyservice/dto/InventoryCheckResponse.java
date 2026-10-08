package com.rikkei.microservice.pharmacyservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryCheckResponse {
    private String medicineCode;
    private String medicineName;
    private Integer availableStock;
    private String warehouseLocation;
    private String status;
    private String message;
}
