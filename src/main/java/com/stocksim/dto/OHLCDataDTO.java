package com.stocksim.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OHLCDataDTO {
    private long timestamp;  // epoch milliseconds for Chart.js
    private double open;
    private double high;
    private double low;
    private double close;
    private long volume;
}
