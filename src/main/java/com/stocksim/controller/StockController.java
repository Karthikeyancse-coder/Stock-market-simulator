package com.stocksim.controller;

import com.stocksim.dto.ApiResponse;
import com.stocksim.dto.OHLCDataDTO;
import com.stocksim.dto.StockDTO;
import com.stocksim.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockDTO>>> getAllStocks() {
        return ResponseEntity.ok(ApiResponse.success(stockService.getAllStocks()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StockDTO>> getStockById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(stockService.getStockDTOById(id)));
    }

    @GetMapping("/{id}/ohlc")
    public ResponseEntity<ApiResponse<List<OHLCDataDTO>>> getStockOHLC(
            @PathVariable Long id,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(ApiResponse.success(stockService.getOHLCData(id, limit)));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getStockHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(stockService.getPriceHistory(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<StockDTO>>> searchStocks(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(stockService.searchStocks(q)));
    }
}
