package com.aidana.wallet_api.repository;

import com.aidana.wallet_api.config.PostgresContainerTest;
import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.User;
import com.aidana.wallet_api.enums.Currency;
import com.aidana.wallet_api.util.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AccountRepositoryTest extends PostgresContainerTest {

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnAccountByIdAndUserId() {

        User user = TestDataFactory.createUser();
        entityManager.persist(user);

        Account account = TestDataFactory.createAccount(user, Currency.USD);
        entityManager.persist(account);

        entityManager.flush();
        entityManager.clear();

        Optional<Account> result = accountRepository.findByIdAndUserId(
                account.getId(),
                user.getId()
        );

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(account.getId());
    }

    @Test
    void shouldReturnAccountsByUserId() {

        User user = TestDataFactory.createUser();
        entityManager.persist(user);

        Account accountFirst = TestDataFactory.createAccount(user, Currency.USD);
        entityManager.persist(accountFirst);

        Account accountSecond = TestDataFactory.createAccount(user, Currency.EUR);
        entityManager.persist(accountSecond);

        entityManager.flush();
        entityManager.clear();

        List<Account> result = accountRepository.findByUserId(user.getId());

        assertThat(result).hasSize(2);
    }
}
