package com.stocksim.scheduler;

import com.stocksim.service.OHLCService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OHLCScheduler {

    private final OHLCService ohlcService;

    @Scheduled(fixedRate = 60000)
    public void closeCandles() {
        try {
            ohlcService.closeAllCandles();
            log.debug("OHLC candles closed and new ones opened");
        } catch (Exception e) {
            log.error("OHLC scheduler error: {}", e.getMessage());
        }
    }
}
