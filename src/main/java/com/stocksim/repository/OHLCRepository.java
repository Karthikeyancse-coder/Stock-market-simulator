package com.stocksim.repository;

import com.stocksim.model.OHLCData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OHLCRepository extends JpaRepository<OHLCData, Long> {
    List<OHLCData> findByStockIdOrderByCandleTimeAsc(Long stockId);
    List<OHLCData> findTop100ByStockIdOrderByCandleTimeDesc(Long stockId);
}
