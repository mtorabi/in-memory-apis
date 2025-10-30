package com.reckless_bank.in_memory_apis.account.application.usecase;

import com.reckless_bank.in_memory_apis.account.application.dto.TransactionRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferResult;
import com.reckless_bank.in_memory_apis.account.domain.model.Account;
import com.reckless_bank.in_memory_apis.common.exception.InvalidTransactionException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WithdrawUseCase {
    
    private final TransferUseCase transferUseCase;
    
    public WithdrawUseCase(TransferUseCase transferUseCase) {
        this.transferUseCase = transferUseCase;
    }

    public Optional<Account> execute(String accountId, TransactionRequest request) {
        // Validate input parameters
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new InvalidTransactionException("withdrawal", null, "Account ID cannot be null or empty");
        }
        
        // Create a TransferRequest for withdrawal (fromAccount = accountId, toAccount = null)
        TransferRequest transferRequest = new TransferRequest(accountId, null, request.amount());
        
        Optional<TransferResult> transferResult = transferUseCase.execute(transferRequest);
        
        if (transferResult.isPresent()) {
            return Optional.of(transferResult.get().fromAccount());
        }
        
        return Optional.empty();
    }
}