package com.aidana.wallet_api.controller;

import com.aidana.wallet_api.DTO.request.UpdateUserRequest;
import com.aidana.wallet_api.DTO.response.AccountResponse;
import com.aidana.wallet_api.DTO.response.TransactionResponse;
import com.aidana.wallet_api.DTO.response.UserResponse;
import com.aidana.wallet_api.service.AccountService;
import com.aidana.wallet_api.service.TransactionService;
import com.aidana.wallet_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping("/users/{userId}")
    public UserResponse getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @GetMapping("/users")
    public List<UserResponse> getUsers(
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        return userService.getUsers(page, size);
    }

    @PutMapping("/users/{userId}")
    public UserResponse updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request
    ) {
        return userService.updateUser(request, userId);
    }

    @PatchMapping("/accounts/{accountId}/block")
    public AccountResponse blockAccount(@PathVariable Long accountId) {
        return accountService.blockAccount(accountId);
    }

    @PatchMapping("/accounts/{accountId}/unblock")
    public AccountResponse unblockAccount(@PathVariable Long accountId) {
        return accountService.unblockAccount(accountId);
    }

    @GetMapping("/accounts")
    public List<AccountResponse> getAccounts(
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        return accountService.getAccounts(page, size);
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> getTransactions(
            @RequestParam Integer page,
            @RequestParam Integer size
    ) {
        return transactionService.getTransactions(page, size);
    }
}
