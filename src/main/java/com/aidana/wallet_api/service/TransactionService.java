package com.aidana.wallet_api.service;

import com.aidana.wallet_api.DTO.request.DepositRequest;
import com.aidana.wallet_api.DTO.request.WithdrawRequest;
import com.aidana.wallet_api.DTO.response.AccountResponse;
import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.Transaction;
import com.aidana.wallet_api.enums.TransactionStatus;
import com.aidana.wallet_api.enums.TransactionType;
import com.aidana.wallet_api.exception.AccountBlockedException;
import com.aidana.wallet_api.exception.InsufficientBalanceException;
import com.aidana.wallet_api.repository.AccountRepository;
import com.aidana.wallet_api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountResponse deposit(Long accountId, Long userId, DepositRequest request) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        if (account.getBlockedAt() != null) {
            throw new AccountBlockedException("Account is blocked");
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
            throw new AccountBlockedException("Account is blocked");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Balance is less than withdraw amount");
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
}
