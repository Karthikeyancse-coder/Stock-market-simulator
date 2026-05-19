package com.stocksim.service;

import com.stocksim.dto.StockDTO;
import com.stocksim.model.Stock;
import com.stocksim.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceSimulatorService {

    private final StockRepository stockRepository;
    private final OHLCService ohlcService;
    private final Random random = new Random();

    private static final double DRIFT = 0.00005;

    @Transactional
    public List<StockDTO> updateAllPrices() {
        List<Stock> stocks = stockRepository.findByIsActiveTrue();

        for (Stock stock : stocks) {
            BigDecimal currentPrice = stock.getCurrentPrice();
            if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) continue;

            // Geometric Brownian Motion
            double shock = random.nextGaussian() * stock.getVolatility().doubleValue();
            double multiplier = Math.exp(DRIFT + shock);
            BigDecimal newPrice = currentPrice
                    .multiply(BigDecimal.valueOf(multiplier))
                    .setScale(2, RoundingMode.HALF_UP);

            // Floor at ₹1
            newPrice = newPrice.max(BigDecimal.ONE);

            stock.setCurrentPrice(newPrice);

            // Update today high/low
            if (stock.getHighToday() == null || newPrice.compareTo(stock.getHighToday()) > 0) {
                stock.setHighToday(newPrice);
            }
            if (stock.getLowToday() == null || newPrice.compareTo(stock.getLowToday()) < 0) {
                stock.setLowToday(newPrice);
            }

            // Volume spike
            long volumeSpike = (long)(random.nextDouble() * 15000);
            stock.setVolume((stock.getVolume() != null ? stock.getVolume() : 0L) + volumeSpike);

            stockRepository.save(stock);

            // Update current OHLC candle
            ohlcService.updateCurrentCandle(stock);
        }

        return stocks.stream().map(StockDTO::fromEntity).toList();
    }
}
