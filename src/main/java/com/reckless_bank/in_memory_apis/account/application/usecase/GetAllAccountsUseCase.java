package com.reckless_bank.in_memory_apis.account.application.usecase;

import com.reckless_bank.in_memory_apis.account.domain.model.Account;
import com.reckless_bank.in_memory_apis.account.domain.repository.IAccountRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GetAllAccountsUseCase {
    
    private final IAccountRepository accountRepository;
    
    public GetAllAccountsUseCase(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    public Map<String, Account> execute() {
        return accountRepository.findAll();
    }
}