package com.aidana.wallet_api.repository;

import com.aidana.wallet_api.config.PostgresContainerTest;

import com.aidana.wallet_api.entity.RefreshToken;
import com.aidana.wallet_api.entity.User;
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
public class RefreshTokenRepositoryTest extends PostgresContainerTest {

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnRefreshTokenWhenTokenHashExists() {

        String hashedToken = "hashedToken";

        User user = TestDataFactory.createUser();
        entityManager.persist(user);

        RefreshToken refreshToken = TestDataFactory.createRefreshToken(user, hashedToken);
        entityManager.persist(refreshToken);

        entityManager.flush();
        entityManager.clear();

        Optional<RefreshToken> result = refreshTokenRepository.findByTokenHash(hashedToken);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(refreshToken.getId());
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(result.get().getTokenHash()).isEqualTo(hashedToken);
    }

    @Test
    void shouldReturnEmptyWhenTokenHashDoesNotExist() {

        Optional<RefreshToken> result = refreshTokenRepository.findByTokenHash("hashedToken");

        assertThat(result).isEmpty();
    }
}
