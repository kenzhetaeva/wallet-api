package com.aidana.wallet_api.controller;

import com.aidana.wallet_api.DTO.request.WithdrawRequest;
import com.aidana.wallet_api.config.PostgresContainerTest;
import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.User;
import com.aidana.wallet_api.repository.AccountRepository;
import com.aidana.wallet_api.repository.TransactionRepository;
import com.aidana.wallet_api.repository.UserRepository;
import com.aidana.wallet_api.service.JwtService;
import com.aidana.wallet_api.util.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest extends PostgresContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtService jwtService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Test
    void shouldWithdrawMoney() throws Exception {

        User user = userRepository.save(TestDataFactory.createUser());

        Account account = TestDataFactory.createAccount(user);
        account.setBalance(BigDecimal.valueOf(1000));
        account = accountRepository.save(account);

        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(BigDecimal.valueOf(100));

        mockMvc.perform(post("/accounts/{accountId}/withdraw", account.getId())
                .header(HttpHeaders.AUTHORIZATION, createToken(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(100));

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();

        assertThat(updatedAccount.getBalance()).isEqualTo("900.00");
        assertThat(transactionRepository.findAll()).hasSize(1);
    }

    @Test
    void shouldReturn401WhenTokenIsMissing() throws Exception {

        User user = userRepository.save(TestDataFactory.createUser());

        Account account = TestDataFactory.createAccount(user);
        account.setBalance(BigDecimal.valueOf(1000));
        account = accountRepository.save(account);

        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(BigDecimal.valueOf(100));

        mockMvc.perform(post("/accounts/{accountId}/withdraw", account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(status().is4xxClientError());

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();

        assertThat(updatedAccount.getBalance()).isEqualTo("1000.00");
        assertThat(transactionRepository.findAll()).hasSize(0);
    }

    private String createToken(User user) {
        return "Bearer " + jwtService.generateAccessToken(user);
    }
}
