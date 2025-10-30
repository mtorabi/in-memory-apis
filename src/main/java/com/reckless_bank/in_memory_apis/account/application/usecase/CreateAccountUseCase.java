package com.reckless_bank.in_memory_apis.account.application.usecase;

import com.reckless_bank.in_memory_apis.account.application.dto.CreateAccountRequest;
import com.reckless_bank.in_memory_apis.account.domain.model.Account;
import com.reckless_bank.in_memory_apis.account.domain.repository.IAccountRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateAccountUseCase {
    
    private final IAccountRepository accountRepository;
    
    public CreateAccountUseCase(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    
    public Account execute(CreateAccountRequest request) {
        String accountId = accountRepository.generateNextAccountId();
        Account newAccount = new Account(accountId, request.accountHolder(), request.initialBalance());
        return accountRepository.save(newAccount);
    }
}