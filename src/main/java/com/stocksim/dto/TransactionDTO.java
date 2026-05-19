package com.stocksim.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class TransactionDTO {
    private Long id;
    private String symbol;
    private String companyName;
    private String type;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private BigDecimal realizedPnl;
    private LocalDateTime createdAt;
    private String formattedDate;

    public void setCreatedAtFormatted(LocalDateTime dt) {
        this.createdAt = dt;
        if (dt != null) {
            this.formattedDate = dt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        }
    }
}
