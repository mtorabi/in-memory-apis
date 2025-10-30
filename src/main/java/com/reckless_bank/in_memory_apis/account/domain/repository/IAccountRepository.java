package com.reckless_bank.in_memory_apis.account.domain.repository;

import com.reckless_bank.in_memory_apis.account.domain.model.Account;

import java.util.Map;
import java.util.Optional;

/**
 * Contract interface for Account repository operations.
 * Defines the structure and operations for account data access.
 */
public interface IAccountRepository {
    
    /**
     * Retrieves all accounts in the repository.
     * @return Map of all accounts with accountId as key
     */
    Map<String, Account> findAll();
    
    /**
     * Finds an account by its unique identifier.
     * @param accountId The unique identifier of the account
     * @return Optional containing the account if found, empty otherwise
     */
    Optional<Account> findById(String accountId);
    
    /**
     * Saves an account to the repository.
     * @param account The account to save
     * @return The saved account
     */
    Account save(Account account);
    
    /**
     * Checks if an account exists by its identifier.
     * @param accountId The unique identifier of the account
     * @return true if account exists, false otherwise
     */
    boolean existsById(String accountId);
    
    /**
     * Deletes an account by its identifier.
     * @param accountId The unique identifier of the account to delete
     */
    void deleteById(String accountId);
    
    /**
     * Returns the total number of accounts in the repository.
     * @return The count of accounts
     */
    int count();
    
    /**
     * Generates the next available account identifier.
     * @return A unique account identifier
     */
    String generateNextAccountId();
    
    /**
     * Removes all accounts from the repository.
     * Useful for testing and cleanup operations.
     */
    void clear();
    
    /**
     * Checks if the repository contains any accounts.
     * @return true if repository is empty, false otherwise
     */
    boolean isEmpty();
}