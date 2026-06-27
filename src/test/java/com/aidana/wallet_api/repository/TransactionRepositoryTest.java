package com.aidana.wallet_api.repository;

import com.aidana.wallet_api.DTO.projection.AccountStatisticsProjection;
import com.aidana.wallet_api.DTO.projection.TopUserProjection;
import com.aidana.wallet_api.config.PostgresContainerTest;
import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.Transaction;
import com.aidana.wallet_api.entity.User;
import com.aidana.wallet_api.enums.Currency;
import com.aidana.wallet_api.enums.TransactionStatus;
import com.aidana.wallet_api.enums.TransactionType;
import com.aidana.wallet_api.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TransactionRepositoryTest extends PostgresContainerTest {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnOnlyFirstPageOfTransactionsByAccountId() {

        User user = TestDataFactory.createUser();
        entityManager.persist(user);

        Account account = TestDataFactory.createAccount(user);
        entityManager.persist(account);

        for (int i = 0; i < 5; i++) {
            Transaction transaction =
                    TestDataFactory.createTransaction(account, BigDecimal.valueOf(100));
            entityManager.persist(transaction);
        }

        entityManager.flush();
        entityManager.clear();

        Page<Transaction> page = transactionRepository.findByFromAccountIdOrToAccountId(
                account.getId(),
                account.getId(),
                PageRequest.of(0, 2)
        );

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getNumber()).isEqualTo(0);
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(5);
        assertThat(page.getTotalPages()).isEqualTo(3);
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void shouldReturnTopUsersByCompletedTransactions() {

        // Users
        User userA = TestDataFactory.createUser();
        entityManager.persist(userA);

        User userB = TestDataFactory.createUser();
        entityManager.persist(userB);

        User userC = TestDataFactory.createUser();
        entityManager.persist(userC);

        User userD = TestDataFactory.createUser();
        entityManager.persist(userD);

        User userF = TestDataFactory.createUser();
        entityManager.persist(userF);

        // Accounts
        Account accountA = TestDataFactory.createAccount(userA);
        entityManager.persist(accountA);

        Account accountB = TestDataFactory.createAccount(userB);
        entityManager.persist(accountB);

        Account accountC = TestDataFactory.createAccount(userC, Currency.EUR);
        entityManager.persist(accountC);

        Account accountD = TestDataFactory.createAccount(userD);
        entityManager.persist(accountD);

        Account accountF = TestDataFactory.createAccount(userF);
        entityManager.persist(accountF);

        // Transactions

        // Transactions of User & Account A
        Transaction transactionA1 = TestDataFactory.createTransaction(
                accountA,
                BigDecimal.valueOf(100)
        );
        entityManager.persist(transactionA1);

        Transaction transactionA2 = TestDataFactory.createTransaction(
                accountA,
                BigDecimal.valueOf(200)
        );
        entityManager.persist(transactionA2);

        Transaction transactionA3 = TestDataFactory.createTransaction(
                accountA,
                null,
                BigDecimal.valueOf(200),
                TransactionStatus.FAILED,
                TransactionType.WITHDRAW,
                Instant.now()
        );
        entityManager.persist(transactionA3);

        // Transactions of User & Account B
        Transaction transactionB1 = TestDataFactory.createTransaction(
                accountB,
                BigDecimal.valueOf(150)
        );
        entityManager.persist(transactionB1);

        // Transactions of User & Account C
        Transaction transactionC1 = TestDataFactory.createTransaction(
                accountC,
                BigDecimal.valueOf(1000)
        );
        entityManager.persist(transactionC1);

        // Transactions of User & Account D
        Transaction transactionD1 = TestDataFactory.createTransaction(
                accountD,
                null,
                BigDecimal.valueOf(1000),
                TransactionStatus.COMPLETED,
                TransactionType.WITHDRAW,
                Instant.now().minus(2, ChronoUnit.DAYS)
        );
        entityManager.persist(transactionD1);

        // Transactions of User & Account F
        Transaction transactionF1 = TestDataFactory.createTransaction(
                accountF,
                BigDecimal.valueOf(100)
        );
        entityManager.persist(transactionF1);

        entityManager.flush();
        entityManager.clear();

        Page<TopUserProjection> page = transactionRepository.findTopUsers(
                Currency.USD.toString(),
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS),
                PageRequest.of(0, 2)
        );

        assertThat(page.getContent()).hasSize(2);

        assertThat(page.getContent().get(0).getEmail()).isEqualTo(userA.getEmail());
        assertThat(page.getContent().get(0).getTotalTransferred()).isEqualTo("300.00");
        assertThat(page.getContent().get(1).getEmail()).isEqualTo(userB.getEmail());
        assertThat(page.getContent().get(1).getTotalTransferred()).isEqualTo("150.00");
    }

    @Test
    void shouldReturnTransactionStatisticsOfAccount() {

        // Users
        User userA = TestDataFactory.createUser();
        entityManager.persist(userA);

        User userB = TestDataFactory.createUser();
        entityManager.persist(userB);

        // Accounts
        Account accountA = TestDataFactory.createAccount(userA);
        entityManager.persist(accountA);

        Account accountB = TestDataFactory.createAccount(userB);
        entityManager.persist(accountB);

        // Transactions

        // Transactions of User & Account A

        // Deposit transaction 1
        Transaction transactionA1 = TestDataFactory.createTransaction(
                null,
                accountA,
                BigDecimal.valueOf(500),
                TransactionStatus.COMPLETED,
                TransactionType.DEPOSIT,
                Instant.now()
        );
        entityManager.persist(transactionA1);

        // Deposit transaction 2
        Transaction transactionA2 = TestDataFactory.createTransaction(
                null,
                accountA,
                BigDecimal.valueOf(600),
                TransactionStatus.COMPLETED,
                TransactionType.DEPOSIT,
                Instant.now()
        );
        entityManager.persist(transactionA2);

        // Deposit transaction 3
        Transaction transactionA3 = TestDataFactory.createTransaction(
                null,
                accountA,
                BigDecimal.valueOf(500),
                TransactionStatus.FAILED,
                TransactionType.DEPOSIT,
                Instant.now()
        );
        entityManager.persist(transactionA3);

        // Withdraw transaction 1
        Transaction transactionA4 = TestDataFactory.createTransaction(
                null,
                accountA,
                BigDecimal.valueOf(500),
                TransactionStatus.COMPLETED,
                TransactionType.WITHDRAW,
                Instant.now()
        );
        entityManager.persist(transactionA4);

        // Transfer transaction 1
        Transaction transactionA5 = TestDataFactory.createTransaction(
                accountA,
                accountB,
                BigDecimal.valueOf(300),
                TransactionStatus.COMPLETED,
                TransactionType.TRANSFER,
                Instant.now()
        );
        entityManager.persist(transactionA5);

        // Transaction of User & Account B
        Transaction transactionB1 = TestDataFactory.createTransaction(
                null,
                accountB,
                BigDecimal.valueOf(200),
                TransactionStatus.COMPLETED,
                TransactionType.DEPOSIT,
                Instant.now()
        );
        entityManager.persist(transactionB1);

        entityManager.flush();
        entityManager.clear();

        AccountStatisticsProjection result = transactionRepository.getAccountStatistics(
                accountA.getId()
        );

        assertThat(result.getTotalDeposits()).isEqualTo("1100.00");
        assertThat(result.getTotalWithdrawals()).isEqualTo("500.00");
        assertThat(result.getTotalTransfers()).isEqualTo("300.00");
        assertThat(result.getTransactionCount()).isEqualTo(4);
    }
}
