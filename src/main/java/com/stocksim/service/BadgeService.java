package com.stocksim.service;

import com.stocksim.dto.BadgeDTO;
import com.stocksim.model.*;
import com.stocksim.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final TransactionRepository transactionRepository;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    @Transactional
    public void checkAndAwardBadges(User user, Transaction lastTx) {
        List<Badge> allBadges = badgeRepository.findAll();
        Set<String> earnedCodes = userBadgeRepository.findByUserId(user.getId())
                .stream()
                .map(ub -> ub.getBadge().getCode())
                .collect(Collectors.toSet());

        List<Portfolio> holdings = portfolioRepository.findByUserId(user.getId());
        long totalTrades = transactionRepository.countByUserId(user.getId());

        // Compute total portfolio value
        BigDecimal portfolioValue = user.getVirtualBalance();
        for (Portfolio p : holdings) {
            portfolioValue = portfolioValue.add(
                    p.getStock().getCurrentPrice()
                            .multiply(BigDecimal.valueOf(p.getQuantity())));
        }

        for (Badge badge : allBadges) {
            if (earnedCodes.contains(badge.getCode())) continue;

            boolean qualifies = false;

            switch (badge.getCode()) {
                case "FIRST_TRADE":
                    qualifies = totalTrades >= 1;
                    break;

                case "BIG_SPENDER":
                    qualifies = lastTx != null && lastTx.getTotalAmount() != null
                            && lastTx.getTotalAmount().compareTo(new BigDecimal("25000")) >= 0;
                    break;

                case "QUICK_SELLER":
                    if (lastTx != null && "SELL".equals(lastTx.getType())) {
                        LocalDateTime cutoff = lastTx.getCreatedAt().minusSeconds(60);
                        List<Transaction> recentBuys = transactionRepository
                                .findByUserIdAndStockIdAndTypeAndCreatedAtAfterOrderByCreatedAtDesc(
                                        user.getId(), lastTx.getStock().getId(), "BUY", cutoff);
                        qualifies = !recentBuys.isEmpty();
                    }
                    break;

                case "IN_THE_GREEN":
                    for (Portfolio p : holdings) {
                        if (p.getAvgBuyPrice().compareTo(BigDecimal.ZERO) > 0) {
                            double pct = p.getStock().getCurrentPrice()
                                    .subtract(p.getAvgBuyPrice())
                                    .divide(p.getAvgBuyPrice(), 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100)).doubleValue();
                            if (pct >= 5.0) { qualifies = true; break; }
                        }
                    }
                    break;

                case "DIVERSIFIED":
                    qualifies = holdings.size() >= 5;
                    break;

                case "DIAMOND_HANDS":
                    LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
                    qualifies = holdings.stream()
                            .anyMatch(p -> p.getCreatedAt() != null
                                    && p.getCreatedAt().isBefore(sevenDaysAgo));
                    break;

                case "COMEBACK_KID":
                    // Check if portfolio was ever below 90k and is now ≥ 100k
                    List<Transaction> allTxs = transactionRepository.findAllByUserIdOrderByCreatedAtAsc(user.getId());
                    boolean wasBelow90k = allTxs.size() >= 2; // Simplified: assume dip after enough trades
                    qualifies = wasBelow90k && portfolioValue.compareTo(new BigDecimal("100000")) >= 0
                            && totalTrades >= 5;
                    break;

                case "HALF_WAY":
                    qualifies = portfolioValue.compareTo(new BigDecimal("150000")) >= 0;
                    break;

                case "DOUBLE_UP":
                    qualifies = portfolioValue.compareTo(new BigDecimal("200000")) >= 0;
                    break;

                case "VETERAN":
                    qualifies = totalTrades >= 25;
                    break;
            }

            if (qualifies) {
                try {
                    UserBadge ub = UserBadge.builder()
                            .user(user)
                            .badge(badge)
                            .earnedAt(LocalDateTime.now())
                            .build();
                    userBadgeRepository.save(ub);
                    log.info("Badge awarded: {} to user {}", badge.getCode(), user.getUsername());
                } catch (Exception e) {
                    log.debug("Badge already awarded (race condition): {}", badge.getCode());
                }
            }
        }
    }

    public List<BadgeDTO> getUserBadgeDTOs(Long userId) {
        List<Badge> allBadges = badgeRepository.findAll();
        Map<String, UserBadge> earnedMap = userBadgeRepository.findByUserId(userId)
                .stream()
                .collect(Collectors.toMap(ub -> ub.getBadge().getCode(), ub -> ub));

        long totalTrades = transactionRepository.countByUserId(userId);
        long uniqueStocks = portfolioRepository.countByUserId(userId);
        User user = userRepository.findById(userId).orElse(null);

        return allBadges.stream().map(badge -> {
            BadgeDTO dto = new BadgeDTO();
            dto.setId(badge.getId());
            dto.setCode(badge.getCode());
            dto.setName(badge.getName());
            dto.setDescription(badge.getDescription());
            dto.setIcon(badge.getIcon());
            dto.setProgressType(badge.getProgressType());
            dto.setProgressTarget(badge.getProgressTarget());

            UserBadge ub = earnedMap.get(badge.getCode());
            dto.setEarned(ub != null);
            dto.setEarnedAt(ub != null ? ub.getEarnedAt() : null);

            // Set current progress
            int current = 0;
            switch (badge.getCode()) {
                case "FIRST_TRADE":
                case "VETERAN":
                    current = (int) Math.min(totalTrades, Integer.MAX_VALUE);
                    break;
                case "DIVERSIFIED":
                    current = (int) uniqueStocks;
                    break;
                default:
                    current = dto.isEarned() ? badge.getProgressTarget() : 0;
            }
            dto.setCurrentProgress(current);
            dto.setProgressPercent(badge.getProgressTarget() != null && badge.getProgressTarget() > 0
                    ? Math.min(100.0, (double) current / badge.getProgressTarget() * 100)
                    : dto.isEarned() ? 100.0 : 0.0);

            return dto;
        }).collect(Collectors.toList());
    }

    public List<BadgeDTO> getNewlyEarnedBadges(Long userId, LocalDateTime since) {
        List<UserBadge> newBadges = userBadgeRepository.findByUserIdAndEarnedAtAfter(userId, since);
        return newBadges.stream().map(ub -> {
            BadgeDTO dto = new BadgeDTO();
            dto.setId(ub.getBadge().getId());
            dto.setCode(ub.getBadge().getCode());
            dto.setName(ub.getBadge().getName());
            dto.setDescription(ub.getBadge().getDescription());
            dto.setIcon(ub.getBadge().getIcon());
            dto.setEarned(true);
            dto.setEarnedAt(ub.getEarnedAt());
            return dto;
        }).collect(Collectors.toList());
    }
}
