package com.aidana.wallet_api.controller;

import com.aidana.wallet_api.DTO.request.DepositRequest;
import com.aidana.wallet_api.DTO.request.WithdrawRequest;
import com.aidana.wallet_api.DTO.response.AccountResponse;
import com.aidana.wallet_api.security.UserPrincipal;
import com.aidana.wallet_api.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/accounts/{accountId}/deposit")
    public AccountResponse deposit(
            @PathVariable Long accountId,
            @Valid @RequestBody DepositRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return transactionService.deposit(accountId, principal.getUserId(), request);
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public AccountResponse withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody WithdrawRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return transactionService.withdraw(accountId, principal.getUserId(), request);
    }
}
