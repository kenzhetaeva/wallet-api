package com.aidana.wallet_api.controller;

import com.aidana.wallet_api.DTO.request.DepositRequest;
import com.aidana.wallet_api.DTO.request.TransferRequest;
import com.aidana.wallet_api.DTO.request.WithdrawRequest;
import com.aidana.wallet_api.DTO.response.AccountResponse;
import com.aidana.wallet_api.DTO.response.TransactionResponse;
import com.aidana.wallet_api.security.UserPrincipal;
import com.aidana.wallet_api.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/transfer")
    public List<AccountResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return transactionService.transfer(principal.getUserId(), request);
    }

    @GetMapping("/transactions/{transactionId}")
    public TransactionResponse getTransaction(
            @PathVariable Long transactionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return transactionService.getTransaction(principal.getUserId(), transactionId);
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public List<TransactionResponse> getTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return transactionService.getAccountTransactions(accountId, principal.getUserId(), page, size);
    }
}
