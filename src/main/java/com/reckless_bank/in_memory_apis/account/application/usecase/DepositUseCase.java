package com.reckless_bank.in_memory_apis.account.application.usecase;

import com.reckless_bank.in_memory_apis.account.application.dto.TransactionRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferResult;
import com.reckless_bank.in_memory_apis.account.domain.model.Account;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DepositUseCase {
    
    private final TransferUseCase transferUseCase;
    
    public DepositUseCase(TransferUseCase transferUseCase) {
        this.transferUseCase = transferUseCase;
    }
    
    public Optional<Account> execute(String accountId, TransactionRequest request) {
        // Create a TransferRequest for deposit (fromAccount = null, toAccount = accountId)
        TransferRequest transferRequest = new TransferRequest(null, accountId, request.amount());
        
        Optional<TransferResult> transferResult = transferUseCase.execute(transferRequest);
        
        if (transferResult.isPresent()) {
            return Optional.of(transferResult.get().toAccount());
        }
        
        return Optional.empty();
    }
}