package com.reckless_bank.in_memory_apis.account.domain.repository;

import com.reckless_bank.in_memory_apis.account.domain.model.Account;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify the interface contract functionality and demonstrate
 * that different implementations can be used interchangeably.
 */
public class IAccountRepositoryContractTest {

    private IAccountRepository repository;

    @BeforeEach
    void setUp() {
        // Using the singleton instance for testing
        repository = AccountRepository.getInstance();
        repository.clear(); // Start with clean state
    }

    @Test
    public void testRepositoryContractCompliance() {
        // Verify the repository starts empty
        assertTrue(repository.isEmpty(), "Repository should start empty");
        assertEquals(0, repository.count(), "Count should be zero initially");
        
        // Test account creation and save
        Account testAccount = new Account("TEST001", "John Doe", new BigDecimal("1000.00"));
        Account savedAccount = repository.save(testAccount);
        
        assertNotNull(savedAccount, "Saved account should not be null");
        assertEquals(testAccount.accountId(), savedAccount.accountId(), "Account IDs should match");
        assertFalse(repository.isEmpty(), "Repository should not be empty after saving");
        assertEquals(1, repository.count(), "Count should be 1 after saving one account");
        
        // Test findById
        Optional<Account> foundAccount = repository.findById("TEST001");
        assertTrue(foundAccount.isPresent(), "Account should be found");
        assertEquals("John Doe", foundAccount.get().accountHolder(), "Account holder should match");
        
        // Test existsById
        assertTrue(repository.existsById("TEST001"), "Account should exist");
        assertFalse(repository.existsById("NONEXISTENT"), "Non-existent account should not exist");
        
        // Test findAll
        Map<String, Account> allAccounts = repository.findAll();
        assertEquals(1, allAccounts.size(), "Should have one account");
        assertTrue(allAccounts.containsKey("TEST001"), "Should contain our test account");
        
        // Test generateNextAccountId
        String nextId = repository.generateNextAccountId();
        assertNotNull(nextId, "Next ID should not be null");
        assertTrue(nextId.startsWith("ACC"), "ID should follow naming convention");
        
        // Test deleteById
        repository.deleteById("TEST001");
        assertFalse(repository.existsById("TEST001"), "Account should be deleted");
        assertTrue(repository.isEmpty(), "Repository should be empty after deletion");
        
        // Test clear
        repository.save(new Account("TEST002", "Jane Doe", new BigDecimal("500.00")));
        repository.save(new Account("TEST003", "Bob Smith", new BigDecimal("750.00")));
        assertEquals(2, repository.count(), "Should have 2 accounts before clear");
        
        repository.clear();
        assertTrue(repository.isEmpty(), "Repository should be empty after clear");
        assertEquals(0, repository.count(), "Count should be zero after clear");
    }

    @Test
    public void testSingletonInterfaceBehavior() {
        // Test that interface methods work through singleton
        IAccountRepository repo1 = AccountRepository.getInstance();
        IAccountRepository repo2 = AccountRepository.getInstance();
        
        // Both references should point to the same singleton instance
        assertSame(repo1, repo2, "Both references should point to same singleton");
        
        // State should be shared
        Account account = new Account("SHARED001", "Shared User", new BigDecimal("100.00"));
        repo1.save(account);
        
        assertTrue(repo2.existsById("SHARED001"), "Account saved through repo1 should be accessible via repo2");
        assertEquals(1, repo2.count(), "Count should be consistent across references");
        
        // Clean up
        repo1.clear();
    }

    @Test
    public void testDefensiveCopyInFindAll() {
        // Add some test data
        repository.save(new Account("ACC001", "User1", new BigDecimal("100.00")));
        repository.save(new Account("ACC002", "User2", new BigDecimal("200.00")));
        
        // Get the map of accounts
        Map<String, Account> accounts = repository.findAll();
        assertEquals(2, accounts.size(), "Should have 2 accounts initially");
        
        // Try to modify the returned map (should not affect internal state)
        accounts.clear();
        
        // Verify internal state is unchanged
        assertEquals(2, repository.count(), "Repository count should be unchanged");
        assertFalse(repository.isEmpty(), "Repository should not be empty");
        
        // Get fresh copy and verify it still has data
        Map<String, Account> freshCopy = repository.findAll();
        assertEquals(2, freshCopy.size(), "Fresh copy should have original data");
        
        // Clean up
        repository.clear();
    }
}