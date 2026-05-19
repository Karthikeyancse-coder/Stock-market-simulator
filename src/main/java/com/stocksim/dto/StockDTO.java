package com.stocksim.dto;

import com.stocksim.model.Stock;
import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
public class StockDTO {
    private Long id;
    private String symbol;
    private String companyName;
    private String sector;
    private BigDecimal currentPrice;
    private BigDecimal prevClose;
    private BigDecimal openPrice;
    private BigDecimal highToday;
    private BigDecimal lowToday;
    private Long volume;
    private double changePercent;
    private BigDecimal changeAmount;
    private boolean isGainer;

    public static StockDTO fromEntity(Stock s) {
        StockDTO dto = new StockDTO();
        dto.setId(s.getId());
        dto.setSymbol(s.getSymbol());
        dto.setCompanyName(s.getCompanyName());
        dto.setSector(s.getSector());
        dto.setCurrentPrice(s.getCurrentPrice());
        dto.setPrevClose(s.getPrevClose());
        dto.setOpenPrice(s.getOpenPrice());
        dto.setHighToday(s.getHighToday());
        dto.setLowToday(s.getLowToday());
        dto.setVolume(s.getVolume());

        if (s.getPrevClose() != null && s.getPrevClose().compareTo(BigDecimal.ZERO) != 0
                && s.getCurrentPrice() != null) {
            BigDecimal change = s.getCurrentPrice().subtract(s.getPrevClose());
            dto.setChangeAmount(change.setScale(2, RoundingMode.HALF_UP));
            dto.setChangePercent(change.divide(s.getPrevClose(), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue());
            dto.setGainer(dto.getChangePercent() >= 0);
        } else {
            dto.setChangeAmount(BigDecimal.ZERO);
            dto.setChangePercent(0.0);
            dto.setGainer(true);
        }
        return dto;
    }
}
