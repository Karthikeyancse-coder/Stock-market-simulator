package com.stocksim.controller;

import com.stocksim.dto.ApiResponse;
import com.stocksim.dto.TradeRequest;
import com.stocksim.repository.UserRepository;
import com.stocksim.service.TradeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/trade")
@RequiredArgsConstructor
public class TradeController {

    private final TradeService tradeService;
    private final UserRepository userRepository;

    @PostMapping("/buy")
    public ResponseEntity<ApiResponse<Map<String, Object>>> buyStock(@Valid @RequestBody TradeRequest request) {
        Long userId = getUserId();
        return ResponseEntity.ok(tradeService.buyStock(userId, request.getStockId(), request.getQuantity()));
    }

    @PostMapping("/sell")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sellStock(@Valid @RequestBody TradeRequest request) {
        Long userId = getUserId();
        return ResponseEntity.ok(tradeService.sellStock(userId, request.getStockId(), request.getQuantity()));
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
