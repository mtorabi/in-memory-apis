package com.reckless_bank.in_memory_apis.account.infrastructure.rest.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.reckless_bank.in_memory_apis.account.application.dto.CreateAccountRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransactionRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferResult;
import com.reckless_bank.in_memory_apis.account.application.usecase.CreateAccountUseCase;
import com.reckless_bank.in_memory_apis.account.application.usecase.DepositUseCase;
import com.reckless_bank.in_memory_apis.account.application.usecase.GetAccountBalanceUseCase;
import com.reckless_bank.in_memory_apis.account.application.usecase.GetAccountUseCase;
import com.reckless_bank.in_memory_apis.account.application.usecase.GetAllAccountsUseCase;
import com.reckless_bank.in_memory_apis.account.application.usecase.TransferUseCase;
import com.reckless_bank.in_memory_apis.account.application.usecase.WithdrawUseCase;
import com.reckless_bank.in_memory_apis.account.domain.model.Account;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Account Controller for managing bank accounts
 * Provides REST endpoints for account operations
 */
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final GetAllAccountsUseCase getAllAccountsUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final CreateAccountUseCase createAccountUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final GetAccountBalanceUseCase getAccountBalanceUseCase;
    private final TransferUseCase transferUseCase;

    // Constructor injection for the use cases
    public AccountController(GetAllAccountsUseCase getAllAccountsUseCase,
                           GetAccountUseCase getAccountUseCase,
                           CreateAccountUseCase createAccountUseCase,
                           DepositUseCase depositUseCase,
                           WithdrawUseCase withdrawUseCase,
                           GetAccountBalanceUseCase getAccountBalanceUseCase,
                           TransferUseCase transferUseCase) {
        this.getAllAccountsUseCase = getAllAccountsUseCase;
        this.getAccountUseCase = getAccountUseCase;
        this.createAccountUseCase = createAccountUseCase;
        this.depositUseCase = depositUseCase;
        this.withdrawUseCase = withdrawUseCase;
        this.getAccountBalanceUseCase = getAccountBalanceUseCase;
        this.transferUseCase = transferUseCase;
    }

    @GetMapping
    public ResponseEntity<Map<String, Account>> getAllAccounts() {
        return ResponseEntity.ok(getAllAccountsUseCase.execute());
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccount(@PathVariable String accountId) {
        Optional<Account> account = getAccountUseCase.execute(accountId);
        if (account.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(account.get());
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody CreateAccountRequest request) {
        Account newAccount = createAccountUseCase.execute(request);
        return ResponseEntity.ok(newAccount);
    }

    @PostMapping("/{accountId}/deposit")
    public ResponseEntity<Account> deposit(@PathVariable String accountId, @RequestBody TransactionRequest request) {
        Optional<Account> result = depositUseCase.execute(accountId, request);
        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result.get());
    }

    @PostMapping("/{accountId}/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(@PathVariable String accountId, @RequestBody TransactionRequest request) {
        Optional<Account> result = withdrawUseCase.execute(accountId, request);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("account", result.get());
        response.put("message", "Withdrawal successful");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable String accountId) {
        Optional<Account> accountOpt = getAccountBalanceUseCase.execute(accountId);
        if (accountOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Account account = accountOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("accountId", accountId);
        response.put("balance", account.balance());
        response.put("accountHolder", account.accountHolder());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transfer(@RequestBody TransferRequest request) {
        Optional<TransferResult> result = transferUseCase.execute(request);
        
        if (result.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        TransferResult transferResult = result.get();
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Transfer successful");
        response.put("fromAccount", transferResult.fromAccount());
        response.put("toAccount", transferResult.toAccount());
        response.put("transferAmount", request.amount());
        return ResponseEntity.ok(response);
    }
}