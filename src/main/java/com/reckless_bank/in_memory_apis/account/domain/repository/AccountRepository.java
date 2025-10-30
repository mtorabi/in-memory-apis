package com.reckless_bank.in_memory_apis.account.domain.repository;

import com.reckless_bank.in_memory_apis.account.domain.model.Account;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

@Repository
@Scope("singleton") // Explicitly declare singleton scope (this is default for Spring beans)
public class AccountRepository implements IAccountRepository {
    
    // Static instance for singleton pattern
    private static volatile IAccountRepository instance;
    
    // In-memory storage for accounts - using ConcurrentHashMap for thread safety
    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    
    // Default constructor for Spring dependency injection
    // Spring will manage this as a singleton automatically
    public AccountRepository() {
        // Spring will use this constructor and manage as singleton
    }
    
    // Thread-safe singleton getInstance method using double-checked locking
    public static IAccountRepository getInstance() {
        if (instance == null) {
            synchronized (AccountRepository.class) {
                if (instance == null) {
                    instance = new AccountRepository();
                }
            }
        }
        return instance;
    }
    
    @Override
    public Map<String, Account> findAll() {
        return new ConcurrentHashMap<>(accounts); // Return defensive copy
    }
    
    @Override
    public Optional<Account> findById(String accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }
    
    @Override
    public Account save(Account account) {
        accounts.put(account.accountId(), account);
        return account;
    }
    
    @Override
    public boolean existsById(String accountId) {
        return accounts.containsKey(accountId);
    }
    
    @Override
    public void deleteById(String accountId) {
        accounts.remove(accountId);
    }
    
    @Override
    public int count() {
        return accounts.size();
    }
    
    @Override
    public String generateNextAccountId() {
        return "ACC" + String.format("%03d", accounts.size() + 1);
    }
    
    @Override
    public void clear() {
        accounts.clear();
    }
    
    @Override
    public boolean isEmpty() {
        return accounts.isEmpty();
    }
}