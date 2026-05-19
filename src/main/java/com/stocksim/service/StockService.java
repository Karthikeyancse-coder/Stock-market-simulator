package com.stocksim.service;

import com.stocksim.dto.OHLCDataDTO;
import com.stocksim.dto.StockDTO;
import com.stocksim.model.Stock;
import com.stocksim.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final OHLCService ohlcService;

    public List<StockDTO> getAllStocks() {
        return stockRepository.findByIsActiveTrue().stream()
                .map(StockDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Stock getStockById(Long id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found"));
    }

    public StockDTO getStockDTOById(Long id) {
        return StockDTO.fromEntity(getStockById(id));
    }

    public List<OHLCDataDTO> getOHLCData(Long stockId, int limit) {
        return ohlcService.getOHLCHistory(stockId, limit);
    }

    public List<Map<String, Object>> getPriceHistory(Long stockId) {
        List<OHLCDataDTO> ohlc = ohlcService.getOHLCHistory(stockId, 100);
        return ohlc.stream().map(d -> {
            Map<String, Object> point = new HashMap<>();
            point.put("t", d.getTimestamp());
            point.put("p", d.getClose());
            return point;
        }).collect(Collectors.toList());
    }

    public List<StockDTO> searchStocks(String query) {
        return stockRepository
                .findByIsActiveTrueAndSymbolContainingIgnoreCaseOrIsActiveTrueAndCompanyNameContainingIgnoreCase(
                        query, query)
                .stream()
                .map(StockDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
