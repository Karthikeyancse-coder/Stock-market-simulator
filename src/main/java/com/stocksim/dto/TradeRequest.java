package com.stocksim.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TradeRequest {
    @NotNull(message = "Stock ID is required")
    private Long stockId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotBlank(message = "Trade type is required")
    private String type; // "BUY" or "SELL"
}
