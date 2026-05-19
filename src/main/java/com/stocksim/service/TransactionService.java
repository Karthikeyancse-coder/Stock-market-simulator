package com.stocksim.service;

import com.stocksim.dto.TransactionDTO;
import com.stocksim.model.Transaction;
import com.stocksim.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public Page<TransactionDTO> getUserTransactions(Long userId, Pageable pageable) {
        Page<Transaction> txPage = transactionRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return txPage.map(this::toDTO);
    }

    private TransactionDTO toDTO(Transaction tx) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(tx.getId());
        dto.setSymbol(tx.getStock().getSymbol());
        dto.setCompanyName(tx.getStock().getCompanyName());
        dto.setType(tx.getType());
        dto.setQuantity(tx.getQuantity());
        dto.setPrice(tx.getPrice());
        dto.setTotalAmount(tx.getTotalAmount());
        dto.setRealizedPnl(tx.getRealizedPnl());
        dto.setCreatedAtFormatted(tx.getCreatedAt());
        return dto;
    }
}
