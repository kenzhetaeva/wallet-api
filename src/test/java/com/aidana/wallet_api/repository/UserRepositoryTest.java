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

import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest extends PostgresContainerTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnTrueWhenEmailExists() {

        User user = TestDataFactory.createUser();
        entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();

        boolean result = userRepository.existsByEmail(user.getEmail());

        assertThat(result).isEqualTo(true);
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExists() {

        boolean result = userRepository.existsByEmail("email@mail.com");

        assertThat(result).isEqualTo(false);
    }

    @Test
    void shouldReturnUserWhenEmailExists() {

        User user = TestDataFactory.createUser();
        entityManager.persist(user);

        entityManager.flush();
        entityManager.clear();

        Optional<User> result = userRepository.findByEmail(user.getEmail());

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {

        Optional<User> result = userRepository.findByEmail("email@mail.com");

        assertThat(result).isEmpty();
    }
}
