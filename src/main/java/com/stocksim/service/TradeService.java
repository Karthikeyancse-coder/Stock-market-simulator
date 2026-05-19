package com.stocksim.service;

import com.stocksim.dto.ApiResponse;
import com.stocksim.model.*;
import com.stocksim.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final PortfolioRepository portfolioRepository;
    private final TransactionRepository transactionRepository;
    private final MarketConfigRepository marketConfigRepository;
    private final BadgeService badgeService;

    @Transactional
    public ApiResponse<Map<String, Object>> buyStock(Long userId, Long stockId, Integer quantity) {
        // 1. Check market open
        MarketConfig config = marketConfigRepository.findById(1L)
                .orElse(MarketConfig.builder().isMarketOpen(true).build());
        if (!config.getIsMarketOpen()) {
            throw new RuntimeException("Market is currently closed");
        }

        // 2. Load user and stock
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        // 3. Check balance
        BigDecimal totalCost = stock.getCurrentPrice()
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        if (user.getVirtualBalance().compareTo(totalCost) < 0) {
            throw new RuntimeException("Insufficient balance to complete this trade");
        }

        // 4. Deduct balance
        user.setVirtualBalance(user.getVirtualBalance().subtract(totalCost));

        // 5. Upsert portfolio
        Optional<Portfolio> existingOpt = portfolioRepository.findByUserIdAndStockId(userId, stockId);
        Portfolio portfolio;
        if (existingOpt.isPresent()) {
            portfolio = existingOpt.get();
            // Weighted average price
            BigDecimal existingValue = portfolio.getAvgBuyPrice()
                    .multiply(BigDecimal.valueOf(portfolio.getQuantity()));
            BigDecimal newValue = stock.getCurrentPrice()
                    .multiply(BigDecimal.valueOf(quantity));
            int newQty = portfolio.getQuantity() + quantity;
            BigDecimal newAvg = existingValue.add(newValue)
                    .divide(BigDecimal.valueOf(newQty), 2, RoundingMode.HALF_UP);
            portfolio.setQuantity(newQty);
            portfolio.setAvgBuyPrice(newAvg);
        } else {
            portfolio = Portfolio.builder()
                    .user(user)
                    .stock(stock)
                    .quantity(quantity)
                    .avgBuyPrice(stock.getCurrentPrice())
                    .build();
        }
        portfolioRepository.save(portfolio);

        // 6. Create transaction
        Transaction tx = Transaction.builder()
                .user(user)
                .stock(stock)
                .type("BUY")
                .quantity(quantity)
                .price(stock.getCurrentPrice())
                .totalAmount(totalCost)
                .realizedPnl(null)
                .build();
        transactionRepository.save(tx);

        // 7. Save user
        userRepository.save(user);

        // 8. Check badges
        try {
            badgeService.checkAndAwardBadges(user, tx);
        } catch (Exception e) {
            log.warn("Badge check failed: {}", e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("newBalance", user.getVirtualBalance());
        result.put("sharesOwned", portfolio.getQuantity());
        result.put("stockSymbol", stock.getSymbol());
        result.put("totalCost", totalCost);

        return ApiResponse.success("Buy order executed successfully", result);
    }

    @Transactional
    public ApiResponse<Map<String, Object>> sellStock(Long userId, Long stockId, Integer quantity) {
        // 1. Check market
        MarketConfig config = marketConfigRepository.findById(1L)
                .orElse(MarketConfig.builder().isMarketOpen(true).build());
        if (!config.getIsMarketOpen()) {
            throw new RuntimeException("Market is currently closed");
        }

        // 2. Load entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));
        Portfolio portfolio = portfolioRepository.findByUserIdAndStockId(userId, stockId)
                .orElseThrow(() -> new RuntimeException("You don't own any shares of this stock"));

        // 3. Validate quantity
        if (portfolio.getQuantity() < quantity) {
            throw new RuntimeException(
                    "Insufficient shares. You own " + portfolio.getQuantity() + " shares of " + stock.getSymbol());
        }

        // 4. Compute sale amount and P&L
        BigDecimal saleAmount = stock.getCurrentPrice()
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal realizedPnl = stock.getCurrentPrice()
                .subtract(portfolio.getAvgBuyPrice())
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(2, RoundingMode.HALF_UP);

        // 5. Add proceeds to balance
        user.setVirtualBalance(user.getVirtualBalance().add(saleAmount));

        // 6. Update portfolio
        if (portfolio.getQuantity().equals(quantity)) {
            portfolioRepository.delete(portfolio);
        } else {
            portfolio.setQuantity(portfolio.getQuantity() - quantity);
            portfolioRepository.save(portfolio);
        }

        // 7. Create transaction
        Transaction tx = Transaction.builder()
                .user(user)
                .stock(stock)
                .type("SELL")
                .quantity(quantity)
                .price(stock.getCurrentPrice())
                .totalAmount(saleAmount)
                .realizedPnl(realizedPnl)
                .build();
        transactionRepository.save(tx);

        // 8. Save user
        userRepository.save(user);

        // 9. Check badges
        try {
            badgeService.checkAndAwardBadges(user, tx);
        } catch (Exception e) {
            log.warn("Badge check failed: {}", e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("newBalance", user.getVirtualBalance());
        result.put("saleAmount", saleAmount);
        result.put("realizedPnl", realizedPnl);
        result.put("stockSymbol", stock.getSymbol());

        return ApiResponse.success("Sell order executed successfully", result);
    }
}
