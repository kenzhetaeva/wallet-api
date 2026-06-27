package com.aidana.wallet_api.repository;

import com.aidana.wallet_api.config.PostgresContainerTest;
import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.Transaction;
import com.aidana.wallet_api.entity.User;
import com.aidana.wallet_api.enums.Currency;
import com.aidana.wallet_api.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TransactionRepositoryTest extends PostgresContainerTest {

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnOnlyFirstPage() {

        User user = TestDataFactory.createUser();
        entityManager.persist(user);

        Account account = TestDataFactory.createAccount(user, Currency.USD);
        entityManager.persist(account);

        for (int i = 0; i < 5; i++) {
            Transaction transaction = TestDataFactory.createTransaction(account);
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
}
