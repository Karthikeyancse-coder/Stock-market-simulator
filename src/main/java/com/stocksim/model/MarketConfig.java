package com.stocksim.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "market_config")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_market_open")
    @Builder.Default
    private Boolean isMarketOpen = true;

    @Column(name = "update_interval_sec")
    @Builder.Default
    private Integer updateIntervalSec = 5;

    @Column(name = "candle_interval_sec")
    @Builder.Default
    private Integer candleIntervalSec = 60;
}
