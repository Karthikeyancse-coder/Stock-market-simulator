package com.stocksim.scheduler;

import com.stocksim.dto.StockDTO;
import com.stocksim.model.MarketConfig;
import com.stocksim.repository.MarketConfigRepository;
import com.stocksim.service.PriceSimulatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PriceUpdateScheduler {

    private final PriceSimulatorService priceSimulatorService;
    private final SimpMessagingTemplate messagingTemplate;
    private final MarketConfigRepository marketConfigRepository;

    @Scheduled(fixedRate = 5000)
    public void updatePrices() {
        try {
            MarketConfig config = marketConfigRepository.findById(1L)
                    .orElse(MarketConfig.builder().isMarketOpen(true).build());

            if (Boolean.TRUE.equals(config.getIsMarketOpen())) {
                List<StockDTO> updated = priceSimulatorService.updateAllPrices();
                messagingTemplate.convertAndSend("/topic/prices", updated);
                log.debug("Broadcast price update for {} stocks", updated.size());
            }
        } catch (Exception e) {
            log.error("Price update scheduler error: {}", e.getMessage());
        }
    }
}
