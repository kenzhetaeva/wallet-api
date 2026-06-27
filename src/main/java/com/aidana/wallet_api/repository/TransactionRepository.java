package com.aidana.wallet_api.repository;

import com.aidana.wallet_api.DTO.projection.AccountStatisticsProjection;
import com.aidana.wallet_api.DTO.projection.TopUserProjection;
import com.aidana.wallet_api.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByFromAccountIdOrToAccountId(
            Long fromAccountId,
            Long toAccountId,
            Pageable pageable
    );

    @Query(value = """
        SELECT
            u.id AS userId,
            u.email AS email,
            SUM(t.amount) AS totalTransferred
        FROM transactions t
        INNER JOIN accounts a
            ON a.id = t.from_account_id
        INNER JOIN users u
            ON u.id = a.user_id
        WHERE t.status = 'COMPLETED'
            AND a.currency = :currency
            AND t.created_at >= :from
            AND t.created_at < :to
        GROUP BY u.id
        ORDER BY totalTransferred DESC
        """, nativeQuery = true)
    Page<TopUserProjection> findTopUsers(
            @Param("currency") String currency,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query(value = """
    SELECT
        COALESCE(SUM(
            CASE
                WHEN type = 'DEPOSIT' THEN amount
                ELSE 0
            END
        ), 0) AS totalDeposits,

        COALESCE(SUM(
            CASE
                WHEN type = 'WITHDRAW' THEN amount
                ELSE 0
            END
        ), 0) AS totalWithdrawals,

        COALESCE(SUM(
            CASE
                WHEN type = 'TRANSFER' THEN amount
                ELSE 0
            END
        ), 0) AS totalTransfers,

        COUNT(*) AS transactionCount
    FROM transactions
    WHERE status = 'COMPLETED'
      AND (from_account_id = :accountId OR to_account_id = :accountId)
    """, nativeQuery = true)
    AccountStatisticsProjection getAccountStatistics(Long accountId);
}
