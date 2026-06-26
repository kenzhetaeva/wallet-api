package com.aidana.wallet_api.service;

import com.aidana.wallet_api.DTO.request.DepositRequest;
import com.aidana.wallet_api.DTO.request.TransferRequest;
import com.aidana.wallet_api.DTO.request.WithdrawRequest;
import com.aidana.wallet_api.DTO.response.TransactionResponse;
import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.Transaction;
import com.aidana.wallet_api.entity.User;
import com.aidana.wallet_api.enums.Currency;
import com.aidana.wallet_api.enums.TransactionStatus;
import com.aidana.wallet_api.enums.TransactionType;
import com.aidana.wallet_api.exception.AccountBlockedException;
import com.aidana.wallet_api.exception.CurrencyMismatchException;
import com.aidana.wallet_api.exception.InsufficientBalanceException;
import com.aidana.wallet_api.exception.InvalidAccountsException;
import com.aidana.wallet_api.repository.AccountRepository;
import com.aidana.wallet_api.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldReturnTransaction() {

        Long userId = 1L;
        Long transactionId = 1L;

        User user1 = new User();
        user1.setId(userId);

        User user2 = new User();
        user2.setId(2L);

        Account fromAccount = new Account();
        fromAccount.setUser(user1);

        Account toAccount = new Account();
        toAccount.setUser(user2);

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);

        TransactionResponse transactionResponse = new TransactionResponse(transaction);

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        TransactionResponse result = transactionService.getTransaction(userId, transactionId);

        assertEquals(transactionResponse, result);

        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void shouldThrowWhenTransactionNotBelongToCurrentUser() {

        Long userId = 1L;
        Long transactionId = 1L;

        User user2 = new User();
        user2.setId(2L);

        User user3 = new User();
        user3.setId(3L);

        Account toAccount = new Account();
        toAccount.setUser(user2);

        Account fromAccount = new Account();
        fromAccount.setUser(user3);

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        assertThrows(
                NoSuchElementException.class,
                () -> transactionService.getTransaction(userId, transactionId)
        );

        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void shouldThrowWhenTransactionNotFound() {

        Long userId = 1L;
        Long transactionId = 1L;

        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> transactionService.getTransaction(userId, transactionId)
        );

        verify(transactionRepository).findById(transactionId);
    }

    @Test
    void shouldDepositMoney() {

        Long accountId = 1L;
        DepositRequest request = depositRequest();
        Account account = account(BigDecimal.valueOf(500), null, null);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        TransactionResponse response = transactionService.deposit(accountId, request);

        assertEquals(BigDecimal.valueOf(600), account.getBalance());

        assertEquals(TransactionType.DEPOSIT, response.getType());
        assertEquals(TransactionStatus.COMPLETED, response.getStatus());
        assertEquals(request.getAmount(), response.getAmount());

        verify(accountRepository).findById(accountId);
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldCreateDepositTransaction() {

        Long accountId = 1L;
        DepositRequest request = depositRequest();
        Account account = account(BigDecimal.valueOf(500), null, null);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        transactionService.deposit(accountId, request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction transaction = captor.getValue();

        assertNull(transaction.getFromAccount());
        assertEquals(account, transaction.getToAccount());
        assertEquals(BigDecimal.valueOf(100), transaction.getAmount());
        assertEquals(TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals(TransactionType.DEPOSIT, transaction.getType());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void shouldThrowWhenAccountNotFoundOnDepositMoney() {

        Long accountId = 1L;
        DepositRequest request = depositRequest();

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> transactionService.deposit(accountId, request)
        );

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAccountBlockedOnDepositMoney() {

        Long accountId = 1L;
        DepositRequest request = depositRequest();
        Account account = account(null, null, Instant.now());

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        assertThrows(
                AccountBlockedException.class,
                () -> transactionService.deposit(accountId, request)
        );

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldWithdrawMoney() {

        Long accountId = 1L;
        Long userId = 1L;

        WithdrawRequest request = withdrawRequest();
        Account account = account(BigDecimal.valueOf(500), null, null);

        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));

        TransactionResponse response = transactionService.withdraw(accountId, userId, request);

        assertEquals(BigDecimal.valueOf(400), account.getBalance());

        assertEquals(TransactionType.WITHDRAW, response.getType());
        assertEquals(TransactionStatus.COMPLETED, response.getStatus());
        assertEquals(request.getAmount(), response.getAmount());

        verify(accountRepository).findByIdAndUserId(accountId, userId);
        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldCreateWithdrawTransaction() {

        Long accountId = 1L;
        Long userId = 1L;

        WithdrawRequest request = withdrawRequest();
        Account account = account(BigDecimal.valueOf(500), null, null);

        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));

        transactionService.withdraw(accountId, userId, request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction transaction = captor.getValue();

        assertEquals(account, transaction.getFromAccount());
        assertNull(transaction.getToAccount());
        assertEquals(BigDecimal.valueOf(100), transaction.getAmount());
        assertEquals(TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals(TransactionType.WITHDRAW, transaction.getType());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void shouldThrowWhenAccountNotFoundOnWithdrawMoney() {

        Long accountId = 1L;
        Long userId = 1L;

        WithdrawRequest request = withdrawRequest();

        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> transactionService.withdraw(accountId, userId, request)
        );

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAccountBlockedOnWithdrawMoney() {

        Long accountId = 1L;
        Long userId = 1L;

        WithdrawRequest request = withdrawRequest();
        Account account = account(null, null, Instant.now());

        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));

        assertThrows(
                AccountBlockedException.class,
                () -> transactionService.withdraw(accountId, userId, request)
        );

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenWithdrawalAmountExceedsBalance() {

        Long accountId = 1L;
        Long userId = 1L;

        WithdrawRequest request = withdrawRequest();
        Account account = account(BigDecimal.valueOf(50), null, null);

        when(accountRepository.findByIdAndUserId(accountId, userId))
                .thenReturn(Optional.of(account));

        assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.withdraw(accountId, userId, request)
        );

        verify(accountRepository).findByIdAndUserId(accountId, userId);
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldTransferMoney() {

        Long userId = 1L;

        TransferRequest request = transferRequest(2L);
        Account fromAccount = account(BigDecimal.valueOf(500), Currency.EUR, null);
        Account toAccount = account(BigDecimal.valueOf(100), Currency.EUR, null);

        when(accountRepository.findByIdAndUserId(request.getFromAccountId(), userId))
                .thenReturn(Optional.of(fromAccount));

        when(accountRepository.findById(request.getToAccountId()))
                .thenReturn(Optional.of(toAccount));

        TransactionResponse response = transactionService.transfer(userId, request);

        assertEquals(BigDecimal.valueOf(400), fromAccount.getBalance());
        assertEquals(BigDecimal.valueOf(200), toAccount.getBalance());

        assertEquals(TransactionType.TRANSFER, response.getType());
        assertEquals(TransactionStatus.COMPLETED, response.getStatus());
        assertEquals(request.getAmount(), response.getAmount());

        verify(accountRepository).findByIdAndUserId(request.getFromAccountId(), userId);
        verify(accountRepository).findById(request.getToAccountId());

        InOrder inOrder = inOrder(accountRepository);

        inOrder.verify(accountRepository).save(fromAccount);
        inOrder.verify(accountRepository).save(toAccount);

        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void shouldCreateTransferTransaction() {

        Long userId = 1L;

        TransferRequest request = transferRequest(2L);
        Account fromAccount = account(BigDecimal.valueOf(500), Currency.EUR, null);
        Account toAccount = account(BigDecimal.valueOf(100), Currency.EUR, null);

        when(accountRepository.findByIdAndUserId(request.getFromAccountId(), userId))
                .thenReturn(Optional.of(fromAccount));

        when(accountRepository.findById(request.getToAccountId()))
                .thenReturn(Optional.of(toAccount));

        transactionService.transfer(userId, request);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository).save(captor.capture());

        Transaction transaction = captor.getValue();

        assertEquals(fromAccount, transaction.getFromAccount());
        assertEquals(toAccount, transaction.getToAccount());
        assertEquals(BigDecimal.valueOf(100), transaction.getAmount());
        assertEquals(TransactionStatus.COMPLETED, transaction.getStatus());
        assertEquals(TransactionType.TRANSFER, transaction.getType());
        assertNotNull(transaction.getCreatedAt());
    }

    @Test
    void shouldThrowWhenSourceAccountNotFound() {

        Long userId = 1L;

        TransferRequest request = transferRequest(2L);

        when(accountRepository.findByIdAndUserId(request.getFromAccountId(), userId))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> transactionService.transfer(userId, request)
        );

        verify(accountRepository, never()).findById(request.getToAccountId());
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenDestinationAccountNotFound() {

        Long userId = 1L;

        TransferRequest request = transferRequest(2L);
        Account fromAccount = new Account();

        when(accountRepository.findByIdAndUserId(request.getFromAccountId(), userId))
                .thenReturn(Optional.of(fromAccount));

        when(accountRepository.findById(request.getToAccountId()))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> transactionService.transfer(userId, request)
        );

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAccountBlockedOnTransferMoney() {

        Long userId = 1L;

        TransferRequest request = transferRequest(2L);
        Account fromAccount = account(null, null, null);
        Account toAccount = account(null, null, Instant.now());

        when(accountRepository.findByIdAndUserId(request.getFromAccountId(), userId))
                .thenReturn(Optional.of(fromAccount));

        when(accountRepository.findById(request.getToAccountId()))
                .thenReturn(Optional.of(toAccount));

        assertThrows(
                AccountBlockedException.class,
                () -> transactionService.transfer(userId, request)
        );

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenWithdrawalAmountExceedsBalanceOnTransferMoney() {

        Long userId = 1L;

        TransferRequest request = transferRequest(2L);
        Account fromAccount = account(BigDecimal.valueOf(50), null, null);
        Account toAccount = account(BigDecimal.valueOf(100), null, null);

        when(accountRepository.findByIdAndUserId(request.getFromAccountId(), userId))
                .thenReturn(Optional.of(fromAccount));

        when(accountRepository.findById(request.getToAccountId()))
                .thenReturn(Optional.of(toAccount));

        assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.transfer(userId, request)
        );

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenAccountCurrenciesDiffer() {

        Long userId = 1L;

        TransferRequest request = transferRequest(2L);
        Account fromAccount = account(BigDecimal.valueOf(500), Currency.EUR, null);
        Account toAccount = account(BigDecimal.valueOf(100), Currency.USD, null);

        when(accountRepository.findByIdAndUserId(request.getFromAccountId(), userId))
                .thenReturn(Optional.of(fromAccount));

        when(accountRepository.findById(request.getToAccountId()))
                .thenReturn(Optional.of(toAccount));

        assertThrows(
                CurrencyMismatchException.class,
                () -> transactionService.transfer(userId, request)
        );

        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenSameAccountsProvided() {

        Long userId = 1L;

        TransferRequest request = transferRequest(1L);

        assertThrows(
                InvalidAccountsException.class,
                () -> transactionService.transfer(userId, request)
        );

        verify(accountRepository, never()).findByIdAndUserId(request.getFromAccountId(), userId);
        verify(accountRepository, never()).findById(request.getToAccountId());
        verify(accountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    private DepositRequest depositRequest() {
        DepositRequest request = new DepositRequest();
        request.setAmount(BigDecimal.valueOf(100));

        return request;
    }

    private WithdrawRequest withdrawRequest() {
        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(BigDecimal.valueOf(100));

        return request;
    }

    private TransferRequest transferRequest(Long fromAccountId) {
        TransferRequest request = new TransferRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setToAccountId(1L);
        request.setFromAccountId(fromAccountId);

        return request;
    }

    private Account account(
            BigDecimal balance,
            Currency currency,
            Instant blockedAt
    ) {
        Account account = new Account();
        account.setBalance(balance);
        account.setCurrency(currency);
        account.setBlockedAt(blockedAt);

        return account;
    }
}
