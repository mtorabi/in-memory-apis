package com.reckless_bank.in_memory_apis.account.infrastructure.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reckless_bank.in_memory_apis.account.application.dto.CreateAccountRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransactionRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferRequest;
import com.reckless_bank.in_memory_apis.account.application.dto.TransferResult;
import com.reckless_bank.in_memory_apis.account.application.usecase.*;
import com.reckless_bank.in_memory_apis.account.domain.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private GetAllAccountsUseCase getAllAccountsUseCase;

    @Mock
    private GetAccountUseCase getAccountUseCase;

    @Mock
    private CreateAccountUseCase createAccountUseCase;

    @Mock
    private DepositUseCase depositUseCase;

    @Mock
    private WithdrawUseCase withdrawUseCase;

    @Mock
    private GetAccountBalanceUseCase getAccountBalanceUseCase;

    @Mock
    private TransferUseCase transferUseCase;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllAccounts_ShouldReturnListOfAccounts() throws Exception {
        // Given
        Account account1 = new Account("acc1", "John Doe", new BigDecimal("1000.00"));
        Account account2 = new Account("acc2", "Jane Smith", new BigDecimal("2000.00"));
        
        Map<String, Account> accountsMap = new HashMap<>();
        accountsMap.put("acc1", account1);
        accountsMap.put("acc2", account2);
        
        when(getAllAccountsUseCase.execute()).thenReturn(accountsMap);

        // When & Then
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].accountId", containsInAnyOrder("acc1", "acc2")))
                .andExpect(jsonPath("$[*].accountHolder", containsInAnyOrder("John Doe", "Jane Smith")))
                .andExpect(jsonPath("$[*].balance", containsInAnyOrder(1000.00, 2000.00)));
    }

    @Test
    void getAllAccounts_ShouldReturnEmptyList_WhenNoAccountsExist() throws Exception {
        // Given
        Map<String, Account> emptyMap = new HashMap<>();
        when(getAllAccountsUseCase.execute()).thenReturn(emptyMap);

        // When & Then
        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", isA(java.util.List.class)))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getAccount_ShouldReturnAccount_WhenAccountExists() throws Exception {
        // Given
        String accountId = "acc1";
        Account account = new Account(accountId, "John Doe", new BigDecimal("1000.00"));
        when(getAccountUseCase.execute(accountId)).thenReturn(Optional.of(account));

        // When & Then
        mockMvc.perform(get("/api/accounts/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId", is(accountId)))
                .andExpect(jsonPath("$.accountHolder", is("John Doe")))
                .andExpect(jsonPath("$.balance", is(1000.00)));
    }

    @Test
    void getAccount_ShouldReturnNotFound_WhenAccountDoesNotExist() throws Exception {
        // Given
        String accountId = "nonexistent";
        when(getAccountUseCase.execute(accountId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/accounts/{accountId}", accountId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAccount_ShouldReturnCreatedAccount() throws Exception {
        // Given
        CreateAccountRequest request = new CreateAccountRequest("John Doe", new BigDecimal("1000.00"));
        Account createdAccount = new Account("acc1", "John Doe", new BigDecimal("1000.00"));
        
        when(createAccountUseCase.execute(any(CreateAccountRequest.class))).thenReturn(createdAccount);

        // When & Then
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId", is("acc1")))
                .andExpect(jsonPath("$.accountHolder", is("John Doe")))
                .andExpect(jsonPath("$.balance", is(1000.00)));
    }

    @Test
    void deposit_ShouldReturnUpdatedAccount_WhenAccountExists() throws Exception {
        // Given
        String accountId = "acc1";
        TransactionRequest request = new TransactionRequest(new BigDecimal("500.00"));
        Account updatedAccount = new Account(accountId, "John Doe", new BigDecimal("1500.00"));
        
        when(depositUseCase.execute(anyString(), any(TransactionRequest.class)))
                .thenReturn(Optional.of(updatedAccount));

        // When & Then
        mockMvc.perform(post("/api/accounts/{accountId}/deposit", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId", is(accountId)))
                .andExpect(jsonPath("$.balance", is(1500.00)));
    }

    @Test
    void deposit_ShouldReturnNotFound_WhenAccountDoesNotExist() throws Exception {
        // Given
        String accountId = "nonexistent";
        TransactionRequest request = new TransactionRequest(new BigDecimal("500.00"));
        
        when(depositUseCase.execute(anyString(), any(TransactionRequest.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/accounts/{accountId}/deposit", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void withdraw_ShouldReturnResponseWithMessage_WhenAccountExists() throws Exception {
        // Given
        String accountId = "acc1";
        TransactionRequest request = new TransactionRequest(new BigDecimal("300.00"));
        Account updatedAccount = new Account(accountId, "John Doe", new BigDecimal("700.00"));
        
        when(withdrawUseCase.execute(anyString(), any(TransactionRequest.class)))
                .thenReturn(Optional.of(updatedAccount));

        // When & Then
        mockMvc.perform(post("/api/accounts/{accountId}/withdraw", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.account.accountId", is(accountId)))
                .andExpect(jsonPath("$.account.balance", is(700.00)))
                .andExpect(jsonPath("$.message", is("Withdrawal successful")));
    }

    @Test
    void getBalance_ShouldReturnBalanceInfo_WhenAccountExists() throws Exception {
        // Given
        String accountId = "acc1";
        Account account = new Account(accountId, "John Doe", new BigDecimal("1000.00"));
        
        when(getAccountBalanceUseCase.execute(accountId)).thenReturn(Optional.of(account));

        // When & Then
        mockMvc.perform(get("/api/accounts/{accountId}/balance", accountId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accountId", is(accountId)))
                .andExpect(jsonPath("$.balance", is(1000.00)))
                .andExpect(jsonPath("$.accountHolder", is("John Doe")));
    }

    @Test
    void transfer_ShouldReturnTransferResult_WhenTransferSuccessful() throws Exception {
        // Given
        TransferRequest request = new TransferRequest("acc1", "acc2", new BigDecimal("500.00"));
        Account fromAccount = new Account("acc1", "John Doe", new BigDecimal("500.00"));
        Account toAccount = new Account("acc2", "Jane Smith", new BigDecimal("1500.00"));
        TransferResult transferResult = new TransferResult(fromAccount, toAccount);
        
        when(transferUseCase.execute(any(TransferRequest.class)))
                .thenReturn(Optional.of(transferResult));

        // When & Then
        mockMvc.perform(post("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", is("Transfer successful")))
                .andExpect(jsonPath("$.fromAccount.accountId", is("acc1")))
                .andExpect(jsonPath("$.toAccount.accountId", is("acc2")))
                .andExpect(jsonPath("$.transferAmount", is(500.00)));
    }

    @Test
    void transfer_ShouldReturnBadRequest_WhenTransferFails() throws Exception {
        // Given
        TransferRequest request = new TransferRequest("acc1", "acc2", new BigDecimal("500.00"));
        
        when(transferUseCase.execute(any(TransferRequest.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/api/accounts/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}