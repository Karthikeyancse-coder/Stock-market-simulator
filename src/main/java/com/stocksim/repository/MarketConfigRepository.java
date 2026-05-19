package com.stocksim.repository;

import com.stocksim.model.MarketConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketConfigRepository extends JpaRepository<MarketConfig, Long> {
}
