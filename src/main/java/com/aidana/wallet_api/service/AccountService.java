package com.aidana.wallet_api.service;

import com.aidana.wallet_api.DTO.request.CreateAccountRequest;
import com.aidana.wallet_api.DTO.response.AccountResponse;
import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.User;
import com.aidana.wallet_api.repository.AccountRepository;
import com.aidana.wallet_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AccountResponse createAccount(CreateAccountRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User with this id not found"));

        Account account = new Account();

        account.setUser(user);
        account.setCurrency(request.getCurrency());

        accountRepository.save(account);

        return new AccountResponse(account);
    }

    public List<AccountResponse> getUserAccounts(Long userId) {

        return accountRepository.findByUserId(userId)
                .stream()
                .map(AccountResponse::new)
                .toList();
    }
}
