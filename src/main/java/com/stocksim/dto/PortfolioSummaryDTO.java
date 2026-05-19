package com.stocksim.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PortfolioSummaryDTO {
    private BigDecimal cashBalance;
    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private BigDecimal totalPnl;
    private double totalPnlPercent;
    private List<HoldingDTO> holdings;
    private boolean isProfit;
}
