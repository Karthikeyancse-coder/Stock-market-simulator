package com.stocksim.service;

import com.stocksim.dto.OHLCDataDTO;
import com.stocksim.model.OHLCData;
import com.stocksim.model.Stock;
import com.stocksim.repository.OHLCRepository;
import com.stocksim.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OHLCService {

    private final OHLCRepository ohlcRepository;
    private final StockRepository stockRepository;

    // stockId → current open candle
    private final ConcurrentHashMap<Long, OHLCData> currentCandles = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeCandles() {
        List<Stock> stocks = stockRepository.findByIsActiveTrue();
        for (Stock stock : stocks) {
            openNewCandle(stock, stock.getCurrentPrice());
        }
        log.info("Initialized OHLC candles for {} stocks", stocks.size());
    }

    public void updateCurrentCandle(Stock stock) {
        BigDecimal newPrice = stock.getCurrentPrice();
        OHLCData candle = currentCandles.get(stock.getId());
        if (candle == null) {
            candle = openNewCandle(stock, newPrice);
        }

        // Update high/low/close
        if (newPrice.compareTo(candle.getHighPrice()) > 0) {
            candle.setHighPrice(newPrice);
        }
        if (newPrice.compareTo(candle.getLowPrice()) < 0) {
            candle.setLowPrice(newPrice);
        }
        candle.setClosePrice(newPrice);
        candle.setVolume((candle.getVolume() != null ? candle.getVolume() : 0L)
                + (long)(Math.random() * 5000));
    }

    @Transactional
    public void closeAllCandles() {
        List<Stock> stocks = stockRepository.findByIsActiveTrue();
        for (Stock stock : stocks) {
            OHLCData candle = currentCandles.get(stock.getId());
            if (candle != null) {
                // Save closed candle to DB
                OHLCData saved = OHLCData.builder()
                        .stock(stock)
                        .openPrice(candle.getOpenPrice())
                        .highPrice(candle.getHighPrice())
                        .lowPrice(candle.getLowPrice())
                        .closePrice(candle.getClosePrice())
                        .volume(candle.getVolume())
                        .candleTime(candle.getCandleTime())
                        .build();
                ohlcRepository.save(saved);

                // Open a new candle with close as new open
                openNewCandle(stock, candle.getClosePrice());
            }
        }
        log.debug("Closed and reopened candles for {} stocks", stocks.size());
    }

    private OHLCData openNewCandle(Stock stock, BigDecimal price) {
        OHLCData candle = OHLCData.builder()
                .stock(stock)
                .openPrice(price)
                .highPrice(price)
                .lowPrice(price)
                .closePrice(price)
                .volume(0L)
                .candleTime(LocalDateTime.now())
                .build();
        currentCandles.put(stock.getId(), candle);
        return candle;
    }

    public List<OHLCDataDTO> getOHLCHistory(Long stockId, int limit) {
        List<OHLCData> data = ohlcRepository.findTop100ByStockIdOrderByCandleTimeDesc(stockId);
        // Reverse to get chronological order
        Collections.reverse(data);

        // Append current open candle
        OHLCData currentCandle = currentCandles.get(stockId);

        List<OHLCDataDTO> result = data.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        if (currentCandle != null) {
            result.add(toDTO(currentCandle));
        }

        return result;
    }

    private OHLCDataDTO toDTO(OHLCData d) {
        long ts = d.getCandleTime() != null
                ? d.getCandleTime().toInstant(ZoneOffset.UTC).toEpochMilli()
                : System.currentTimeMillis();
        return new OHLCDataDTO(
                ts,
                d.getOpenPrice() != null ? d.getOpenPrice().doubleValue() : 0,
                d.getHighPrice() != null ? d.getHighPrice().doubleValue() : 0,
                d.getLowPrice() != null ? d.getLowPrice().doubleValue() : 0,
                d.getClosePrice() != null ? d.getClosePrice().doubleValue() : 0,
                d.getVolume() != null ? d.getVolume() : 0L
        );
    }
}
