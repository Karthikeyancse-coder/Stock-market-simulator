package com.stocksim.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class HoldingDTO {
    private Long stockId;
    private String symbol;
    private String companyName;
    private Integer quantity;
    private BigDecimal avgBuyPrice;
    private BigDecimal currentPrice;
    private BigDecimal currentValue;
    private BigDecimal investedValue;
    private BigDecimal unrealizedPnl;
    private double unrealizedPnlPercent;
    private boolean isProfit;
}
