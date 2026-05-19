package com.stocksim.controller;

import com.stocksim.dto.ApiResponse;
import com.stocksim.dto.PortfolioSummaryDTO;
import com.stocksim.repository.UserRepository;
import com.stocksim.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<PortfolioSummaryDTO>> getPortfolio() {
        return ResponseEntity.ok(ApiResponse.success(portfolioService.getSummary(getUserId())));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPortfolioHistory() {
        return ResponseEntity.ok(ApiResponse.success(portfolioService.getPortfolioHistory(getUserId())));
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }
}
