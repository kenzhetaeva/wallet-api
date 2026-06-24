package com.aidana.wallet_api.controller;

import com.aidana.wallet_api.DTO.request.CreateAccountRequest;
import com.aidana.wallet_api.DTO.response.AccountResponse;
import com.aidana.wallet_api.security.UserPrincipal;
import com.aidana.wallet_api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping()
    public AccountResponse createAccount(
            @RequestBody CreateAccountRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return accountService.createAccount(request, principal.getUserId());
    }

    @GetMapping()
    public List<AccountResponse> getUserAccounts(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return accountService.getUserAccounts(principal.getUserId());
    }

    @GetMapping("/{accountId}")
    public AccountResponse getAccount(
            @PathVariable Long accountId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return accountService.getAccount(accountId, principal.getUserId());
    }
}
