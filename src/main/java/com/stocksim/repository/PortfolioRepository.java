package com.stocksim.repository;

import com.stocksim.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUserId(Long userId);
    Optional<Portfolio> findByUserIdAndStockId(Long userId, Long stockId);
    long countByUserId(Long userId);

    @Query("SELECT COUNT(p) FROM Portfolio p WHERE p.user.id = :userId AND p.quantity > 0")
    long countActiveHoldingsByUserId(Long userId);
}
