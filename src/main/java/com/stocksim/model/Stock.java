package com.stocksim.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String symbol;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(length = 50)
    private String sector;

    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "prev_close", precision = 10, scale = 2)
    private BigDecimal prevClose;

    @Column(name = "open_price", precision = 10, scale = 2)
    private BigDecimal openPrice;

    @Column(name = "high_today", precision = 10, scale = 2)
    private BigDecimal highToday;

    @Column(name = "low_today", precision = 10, scale = 2)
    private BigDecimal lowToday;

    private Long volume;

    @Column(precision = 6, scale = 4)
    private BigDecimal volatility;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private double changePercent;

    @Transient
    private BigDecimal changeAmount;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
