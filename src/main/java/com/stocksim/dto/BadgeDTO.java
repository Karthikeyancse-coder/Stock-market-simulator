package com.stocksim.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BadgeDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private boolean earned;
    private LocalDateTime earnedAt;
    private String progressType;
    private Integer progressTarget;
    private Integer currentProgress;
    private double progressPercent;
}
