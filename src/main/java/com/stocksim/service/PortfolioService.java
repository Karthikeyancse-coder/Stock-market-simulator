package com.stocksim.service;

import com.stocksim.dto.HoldingDTO;
import com.stocksim.dto.PortfolioSummaryDTO;
import com.stocksim.model.Portfolio;
import com.stocksim.model.Transaction;
import com.stocksim.model.User;
import com.stocksim.repository.PortfolioRepository;
import com.stocksim.repository.TransactionRepository;
import com.stocksim.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public PortfolioSummaryDTO getSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Portfolio> holdings = portfolioRepository.findByUserId(userId);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        List<HoldingDTO> holdingDTOs = new ArrayList<>();

        for (Portfolio p : holdings) {
            BigDecimal currentPrice = p.getStock().getCurrentPrice();
            BigDecimal invested = p.getAvgBuyPrice()
                    .multiply(BigDecimal.valueOf(p.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal value = currentPrice
                    .multiply(BigDecimal.valueOf(p.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal pnl = value.subtract(invested).setScale(2, RoundingMode.HALF_UP);
            double pnlPct = invested.compareTo(BigDecimal.ZERO) > 0
                    ? pnl.divide(invested, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;

            HoldingDTO h = new HoldingDTO();
            h.setStockId(p.getStock().getId());
            h.setSymbol(p.getStock().getSymbol());
            h.setCompanyName(p.getStock().getCompanyName());
            h.setQuantity(p.getQuantity());
            h.setAvgBuyPrice(p.getAvgBuyPrice());
            h.setCurrentPrice(currentPrice);
            h.setCurrentValue(value);
            h.setInvestedValue(invested);
            h.setUnrealizedPnl(pnl);
            h.setUnrealizedPnlPercent(pnlPct);
            h.setProfit(pnl.compareTo(BigDecimal.ZERO) >= 0);
            holdingDTOs.add(h);

            totalInvested = totalInvested.add(invested);
            currentValue = currentValue.add(value);
        }

        BigDecimal totalPnl = currentValue.subtract(totalInvested).setScale(2, RoundingMode.HALF_UP);
        double totalPnlPct = totalInvested.compareTo(BigDecimal.ZERO) > 0
                ? totalPnl.divide(totalInvested, 6, RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        PortfolioSummaryDTO summary = new PortfolioSummaryDTO();
        summary.setCashBalance(user.getVirtualBalance());
        summary.setTotalInvested(totalInvested);
        summary.setCurrentValue(currentValue);
        summary.setTotalPnl(totalPnl);
        summary.setTotalPnlPercent(totalPnlPct);
        summary.setHoldings(holdingDTOs);
        summary.setProfit(totalPnl.compareTo(BigDecimal.ZERO) >= 0);

        return summary;
    }

    public List<Map<String, Object>> getPortfolioHistory(Long userId) {
        // Build portfolio value snapshots from transaction history
        List<Transaction> txs = transactionRepository.findAllByUserIdOrderByCreatedAtAsc(userId);

        List<Map<String, Object>> history = new ArrayList<>();
        BigDecimal runningCash = new BigDecimal("100000.00");
        Map<Long, BigDecimal[]> stockHoldings = new HashMap<>(); // stockId → [qty, avgPrice]

        for (Transaction tx : txs) {
            Long stockId = tx.getStock().getId();
            BigDecimal price = tx.getPrice();
            int qty = tx.getQuantity();

            if ("BUY".equals(tx.getType())) {
                runningCash = runningCash.subtract(tx.getTotalAmount());
                stockHoldings.merge(stockId,
                        new BigDecimal[]{BigDecimal.valueOf(qty), price},
                        (existing, newVal) -> {
                            BigDecimal newQty = existing[0].add(newVal[0]);
                            BigDecimal newAvg = existing[0].multiply(existing[1])
                                    .add(newVal[0].multiply(newVal[1]))
                                    .divide(newQty, 2, RoundingMode.HALF_UP);
                            return new BigDecimal[]{newQty, newAvg};
                        });
            } else {
                runningCash = runningCash.add(tx.getTotalAmount());
                if (stockHoldings.containsKey(stockId)) {
                    BigDecimal[] current = stockHoldings.get(stockId);
                    BigDecimal newQty = current[0].subtract(BigDecimal.valueOf(qty));
                    if (newQty.compareTo(BigDecimal.ZERO) <= 0) {
                        stockHoldings.remove(stockId);
                    } else {
                        stockHoldings.put(stockId, new BigDecimal[]{newQty, current[1]});
                    }
                }
            }

            // Compute portfolio value at this point
            BigDecimal portfolioValue = runningCash;
            for (Map.Entry<Long, BigDecimal[]> entry : stockHoldings.entrySet()) {
                portfolioValue = portfolioValue.add(
                        entry.getValue()[0].multiply(tx.getStock().getId().equals(entry.getKey())
                                ? price : entry.getValue()[1]));
            }

            Map<String, Object> point = new HashMap<>();
            point.put("t", tx.getCreatedAt() != null
                    ? tx.getCreatedAt().toInstant(java.time.ZoneOffset.UTC).toEpochMilli()
                    : System.currentTimeMillis());
            point.put("v", portfolioValue.setScale(2, RoundingMode.HALF_UP));
            history.add(point);
        }

        return history;
    }
}
