package com.reckless_bank.in_memory_apis.account.domain.repository;

import com.reckless_bank.in_memory_apis.account.domain.model.Account;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AccountRepositorySingletonTest {

    @Autowired
    private IAccountRepository accountRepository1;

    @Autowired
    private IAccountRepository accountRepository2;

    @Test
    public void testSpringManagedSingleton() {
        // Spring should inject the same instance
        assertSame(accountRepository1, accountRepository2, 
                   "Spring should inject the same singleton instance");
    }

    @Test
    public void testStaticSingletonMethod() {
        // Test static getInstance method
        IAccountRepository instance1 = AccountRepository.getInstance();
        IAccountRepository instance2 = AccountRepository.getInstance();
        
        assertSame(instance1, instance2, 
                   "getInstance() should return the same singleton instance");
    }

    @Test
    public void testSingletonStateConsistency() {
        // Test that state is shared across all instances
        IAccountRepository repo1 = AccountRepository.getInstance();
        IAccountRepository repo2 = AccountRepository.getInstance();
        
        // Add account through first instance
        Account testAccount = new Account("TEST001", "Test User", new BigDecimal("100.00"));
        repo1.save(testAccount);
        
        // Verify account exists through second instance
        assertTrue(repo2.existsById("TEST001"), 
                   "Account should be accessible through any singleton instance");
        
        assertEquals(1, repo2.count(), 
                     "Account count should be consistent across singleton instances");
        
        // Clean up
        repo1.clear();
    }
}