package com.stocksim.repository;

import com.stocksim.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);
    long countByUserId(Long userId);
    List<Transaction> findByUserIdAndStockIdAndTypeAndCreatedAtAfterOrderByCreatedAtDesc(
            Long userId, Long stockId, String type, LocalDateTime after);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.stock.id = :stockId AND t.type = 'BUY' ORDER BY t.createdAt DESC")
    List<Transaction> findBuyTransactionsByUserAndStock(Long userId, Long stockId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId ORDER BY t.createdAt ASC")
    List<Transaction> findAllByUserIdOrderByCreatedAtAsc(Long userId);
}
