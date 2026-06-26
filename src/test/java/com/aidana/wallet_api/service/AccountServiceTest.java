package com.aidana.wallet_api.service;

import com.aidana.wallet_api.DTO.request.CreateAccountRequest;
import com.aidana.wallet_api.DTO.response.AccountResponse;
import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.User;
import com.aidana.wallet_api.enums.Currency;
import com.aidana.wallet_api.repository.AccountRepository;
import com.aidana.wallet_api.repository.TransactionRepository;
import com.aidana.wallet_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldCreateAccount() {

        Long userId = 1L;

        CreateAccountRequest request = new CreateAccountRequest();
        request.setCurrency(Currency.KGS);

        User user = new User();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        AccountResponse response = accountService.createAccount(request, userId);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());

        Account account = captor.getValue();
        assertEquals(user, account.getUser());
        assertEquals(request.getCurrency(), account.getCurrency());
        assertNotNull(account.getCreatedAt());

        assertEquals(request.getCurrency(), response.getCurrency());

        verify(userRepository).findById(userId);
    }

    @Test
    void shouldThrowWhenUserNotFound() {

        Long userId = 1L;

        CreateAccountRequest request = new CreateAccountRequest();
        request.setCurrency(Currency.KGS);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> accountService.createAccount(request, userId)
        );

        assertEquals(
                "User not found",
                exception.getMessage()
        );

        verify(userRepository).findById(userId);
        verifyNoInteractions(accountRepository);
    }
}
