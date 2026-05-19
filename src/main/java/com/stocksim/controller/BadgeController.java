package com.stocksim.controller;

import com.stocksim.dto.ApiResponse;
import com.stocksim.dto.BadgeDTO;
import com.stocksim.repository.UserRepository;
import com.stocksim.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BadgeDTO>>> getBadges() {
        return ResponseEntity.ok(ApiResponse.success(badgeService.getUserBadgeDTOs(getUserId())));
    }

    @GetMapping("/new")
    public ResponseEntity<ApiResponse<List<BadgeDTO>>> getNewBadges(@RequestParam String since) {
        try {
            LocalDateTime sinceTime = LocalDateTime.parse(since.replace("Z", ""));
            // If the JS sends ISO string like "2024-03-24T10:00:00.000Z", we handle parsing safely
            return ResponseEntity.ok(ApiResponse.success(badgeService.getNewlyEarnedBadges(getUserId(), sinceTime)));
        } catch (Exception e) {
            // Fallback parsing for different format
            try {
                Instant instant = Instant.parse(since);
                LocalDateTime sinceTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                return ResponseEntity.ok(ApiResponse.success(badgeService.getNewlyEarnedBadges(getUserId(), sinceTime)));
            } catch (Exception ex) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Invalid date format"));
            }
        }
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
