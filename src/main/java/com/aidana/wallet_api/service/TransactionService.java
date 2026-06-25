package com.aidana.wallet_api.service;

import com.aidana.wallet_api.DTO.request.DepositRequest;
import com.aidana.wallet_api.DTO.request.TransferRequest;
import com.aidana.wallet_api.DTO.request.WithdrawRequest;
import com.aidana.wallet_api.DTO.response.AccountResponse;
import com.aidana.wallet_api.DTO.response.TransactionResponse;
import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.Transaction;
import com.aidana.wallet_api.enums.TransactionStatus;
import com.aidana.wallet_api.enums.TransactionType;
import com.aidana.wallet_api.exception.AccountBlockedException;
import com.aidana.wallet_api.exception.InsufficientBalanceException;
import com.aidana.wallet_api.exception.InvalidAccountsException;
import com.aidana.wallet_api.repository.AccountRepository;
import com.aidana.wallet_api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

    public AccountResponse deposit(Long accountId, Long userId, DepositRequest request) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
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

        return new AccountResponse(account);
    }

    public AccountResponse withdraw(Long accountId, Long userId, WithdrawRequest request) {
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

        return new AccountResponse(account);
    }

    public List<AccountResponse> transfer(Long userId, TransferRequest request) {
        if (Objects.equals(request.getFromAccountId(), request.getToAccountId())) {
            throw new InvalidAccountsException();
        }

        Account fromAccount = accountRepository.findByIdAndUserId(request.getFromAccountId(), userId)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        Account toAccount = accountRepository.findByIdAndUserId(request.getToAccountId(), userId)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        if (fromAccount.getBlockedAt() != null || toAccount.getBlockedAt() != null) {
            throw new AccountBlockedException();
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException();
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

        return List.of(new AccountResponse(fromAccount), new AccountResponse(toAccount));
    }
}
