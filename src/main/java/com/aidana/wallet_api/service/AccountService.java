package com.aidana.wallet_api.service;

import com.aidana.wallet_api.DTO.request.CreateAccountRequest;
import com.aidana.wallet_api.DTO.response.AccountResponse;
import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.User;
import com.aidana.wallet_api.repository.AccountRepository;
import com.aidana.wallet_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AccountResponse createAccount(CreateAccountRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Account account = new Account();

        account.setUser(user);
        account.setCurrency(request.getCurrency());
        account.setCreatedAt(Instant.now());

        accountRepository.save(account);

        return new AccountResponse(account);
    }

    public List<AccountResponse> getUserAccounts(Long userId) {

        return accountRepository.findByUserId(userId)
                .stream()
                .map(AccountResponse::new)
                .toList();
    }

    public List<AccountResponse> getAccounts(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").ascending()
        );

        return accountRepository.findAll(pageable)
                .stream()
                .map(AccountResponse::new)
                .toList();
    }

    public AccountResponse getAccount(Long accountId, Long userId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        return new AccountResponse(account);
    }

    public AccountResponse blockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        account.setBlockedAt(Instant.now());
        accountRepository.save(account);

        return new AccountResponse(account);
    }

    public AccountResponse unblockAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        account.setBlockedAt(null);
        accountRepository.save(account);

        return new AccountResponse(account);
    }
}
