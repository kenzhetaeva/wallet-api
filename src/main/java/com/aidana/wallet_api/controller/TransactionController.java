package com.aidana.wallet_api.controller;

import com.aidana.wallet_api.DTO.request.DepositRequest;
import com.aidana.wallet_api.DTO.request.TransferRequest;
import com.aidana.wallet_api.DTO.request.WithdrawRequest;
import com.aidana.wallet_api.DTO.response.TransactionResponse;
import com.aidana.wallet_api.security.UserPrincipal;
import com.aidana.wallet_api.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/accounts/{accountId}/deposit")
    public TransactionResponse deposit(
            @PathVariable Long accountId,
            @Valid @RequestBody DepositRequest request
    ) {
        return transactionService.deposit(accountId, request);
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public TransactionResponse withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody WithdrawRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return transactionService.withdraw(accountId, principal.getUserId(), request);
    }

    @PostMapping("/transfer")
    public TransactionResponse transfer(
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
    public List<TransactionResponse> getAccountTransactions(
            @PathVariable Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return transactionService.getAccountTransactions(accountId, principal.getUserId(), page, size);
    }

    @GetMapping("/accounts/{accountId}/transactions/export")
    public ResponseEntity<byte[]> getAccountTransactionsExport(
            @PathVariable Long accountId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<TransactionResponse> transactions = transactionService.getAccountTransactions(
                accountId,
                principal.getUserId(),
                null,
                null
        );
        byte[] csv = transactionService.export(transactions);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=transactions.csv"
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
