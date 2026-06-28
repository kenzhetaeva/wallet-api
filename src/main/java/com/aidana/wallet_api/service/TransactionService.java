package com.aidana.wallet_api.service;

import com.aidana.wallet_api.DTO.projection.TopUserProjection;
import com.aidana.wallet_api.DTO.request.DepositRequest;
import com.aidana.wallet_api.DTO.request.TransferRequest;
import com.aidana.wallet_api.DTO.request.WithdrawRequest;
import com.aidana.wallet_api.DTO.response.TopUsersResponse;
import com.aidana.wallet_api.DTO.response.TransactionResponse;
import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.Transaction;
import com.aidana.wallet_api.enums.Currency;
import com.aidana.wallet_api.enums.TransactionStatus;
import com.aidana.wallet_api.enums.TransactionType;
import com.aidana.wallet_api.exception.AccountBlockedException;
import com.aidana.wallet_api.exception.CurrencyMismatchException;
import com.aidana.wallet_api.exception.InsufficientBalanceException;
import com.aidana.wallet_api.exception.InvalidAccountsException;
import com.aidana.wallet_api.repository.AccountRepository;
import com.aidana.wallet_api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionResponse getTransaction(Long userId, Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found"));

        boolean isToAccountOwner =
                transaction.getToAccount() != null &&
                Objects.equals(transaction.getToAccount().getUser().getId(), userId);

        boolean isFromAccountOwner =
                transaction.getFromAccount() != null &&
                Objects.equals(transaction.getFromAccount().getUser().getId(), userId);

        if (!isToAccountOwner && !isFromAccountOwner) {
            throw new NoSuchElementException("Transaction not found");
        }

        return new TransactionResponse(transaction);
    }

    public List<TransactionResponse> getTransactions(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").ascending()
        );

        return transactionRepository.findAll(pageable)
                .stream()
                .map(TransactionResponse::new)
                .toList();
    }

    public List<TransactionResponse> getAccountTransactions(
            Long accountId,
            Long userId,
            Integer page,
            Integer size
    ) {
        accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        Pageable pageable = (page == null || size == null)
                ? Pageable.unpaged()
                : PageRequest.of(page, size);

        return transactionRepository.findByFromAccountIdOrToAccountId(accountId, accountId, pageable)
                .stream()
                .map(TransactionResponse::new)
                .toList();
    }

    public byte[] export(List<TransactionResponse> transactions) {

        StringBuilder csv = new StringBuilder();

        csv.append("id,fromAccountId,toAccountId,amount,status,type,createdAt\n");

        for (TransactionResponse transaction : transactions) {
            csv.append(transaction.getId()).append(",");
            csv.append(transaction.getFromAccountId()).append(",");
            csv.append(transaction.getToAccountId()).append(",");
            csv.append(transaction.getAmount()).append(",");
            csv.append(transaction.getStatus()).append(",");
            csv.append(transaction.getType()).append(",");
            csv.append(transaction.getCreatedAt()).append("\n");
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @CacheEvict(value = "accounts", key = "#accountId")
    public TransactionResponse deposit(Long accountId, DepositRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        if (account.getBlockedAt() != null) {
            throw new AccountBlockedException();
        }

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = new Transaction();

        transaction.setFromAccount(null);
        transaction.setToAccount(account);
        transaction.setAmount(request.getAmount());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setCreatedAt(Instant.now());

        transactionRepository.save(transaction);

        return new TransactionResponse(transaction);
    }

    @CacheEvict(value = "accounts", key = "#accountId")
    public TransactionResponse withdraw(Long accountId, Long userId, WithdrawRequest request) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        if (account.getBlockedAt() != null) {
            throw new AccountBlockedException();
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = new Transaction();

        transaction.setFromAccount(account);
        transaction.setToAccount(null);
        transaction.setAmount(request.getAmount());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setCreatedAt(Instant.now());

        transactionRepository.save(transaction);

        return new TransactionResponse(transaction);
    }

    @Caching(evict = {
            @CacheEvict(value = "accounts", key = "#request.fromAccountId"),
            @CacheEvict(value = "accounts", key = "#request.toAccountId")
    })
    public TransactionResponse transfer(Long userId, TransferRequest request) {
        if (Objects.equals(request.getFromAccountId(), request.getToAccountId())) {
            throw new InvalidAccountsException();
        }

        Account fromAccount = accountRepository.findByIdAndUserId(request.getFromAccountId(), userId)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        if (fromAccount.getBlockedAt() != null || toAccount.getBlockedAt() != null) {
            throw new AccountBlockedException();
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException();
        }

        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new CurrencyMismatchException();
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        accountRepository.save(fromAccount);

        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));
        accountRepository.save(toAccount);

        Transaction transaction = new Transaction();

        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(request.getAmount());
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setCreatedAt(Instant.now());

        transactionRepository.save(transaction);

        return new TransactionResponse(transaction);
    }

    public TopUsersResponse getTopUsers(
            Currency currency,
            LocalDate from,
            LocalDate to,
            int limit
    ) {
        Instant fromInstant = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        Page<TopUserProjection> users = transactionRepository.findTopUsers(
                currency.toString(),
                fromInstant,
                toInstant,
                PageRequest.of(0, limit)
        );

        return new TopUsersResponse(currency, fromInstant, toInstant, users.getContent());
    }
}
