package com.stocksim.repository;

import com.stocksim.model.UserBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {
    List<UserBadge> findByUserId(Long userId);
    Optional<UserBadge> findByUserIdAndBadgeId(Long userId, Long badgeId);
    boolean existsByUserIdAndBadgeCode(Long userId, String badgeCode);
    List<UserBadge> findByUserIdAndEarnedAtAfter(Long userId, LocalDateTime since);
}
