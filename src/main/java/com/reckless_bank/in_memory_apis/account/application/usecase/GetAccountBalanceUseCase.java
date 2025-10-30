package com.reckless_bank.in_memory_apis.account.application.usecase;

import com.reckless_bank.in_memory_apis.account.domain.model.Account;
import com.reckless_bank.in_memory_apis.account.domain.repository.IAccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetAccountBalanceUseCase {
    
    private final IAccountRepository accountRepository;
    
    public GetAccountBalanceUseCase(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    public Optional<Account> execute(String accountId) {
        return accountRepository.findById(accountId);
    }
}