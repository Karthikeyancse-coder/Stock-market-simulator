package com.stocksim.repository;

import com.stocksim.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    List<Stock> findByIsActiveTrue();
    Optional<Stock> findBySymbol(String symbol);
    List<Stock> findByIsActiveTrueAndSymbolContainingIgnoreCaseOrIsActiveTrueAndCompanyNameContainingIgnoreCase(
            String symbol, String companyName);
}
