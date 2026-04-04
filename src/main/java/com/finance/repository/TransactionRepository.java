package com.finance.repository;

import com.finance.domain.transaction.Transaction;
import com.finance.domain.transaction.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // filtered list (not deleted)
    @Query("""
            SELECT t FROM Transaction t
            WHERE t.deleted = false
              AND (:type IS NULL OR t.type = :type)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
              AND (:from IS NULL OR t.date >= :from)
              AND (:to IS NULL OR t.date <= :to)
            ORDER BY t.date DESC
            """)
    Page<Transaction> findAllFiltered(
            @Param("type") TransactionType type,
            @Param("categoryId") Long categoryId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable);

    // total by type
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.deleted = false AND t.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);

    // total by type within date range
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.deleted = false AND t.type = :type
              AND t.date >= :from AND t.date <= :to
            """)
    BigDecimal sumByTypeAndDateRange(
            @Param("type") TransactionType type,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // category-wise totals
    @Query("""
            SELECT t.category.name, t.type, COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.deleted = false AND t.category IS NOT NULL
            GROUP BY t.category.name, t.type
            ORDER BY SUM(t.amount) DESC
            """)
    List<Object[]> categoryWiseTotals();

    // monthly trend: year, month, type, total
    @Query("""
            SELECT YEAR(t.date), MONTH(t.date), t.type, COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.deleted = false AND t.date >= :from
            GROUP BY YEAR(t.date), MONTH(t.date), t.type
            ORDER BY YEAR(t.date), MONTH(t.date)
            """)
    List<Object[]> monthlyTrend(@Param("from") LocalDate from);

    // recent transactions (not deleted)
    @Query("SELECT t FROM Transaction t WHERE t.deleted = false ORDER BY t.date DESC, t.createdAt DESC")
    List<Transaction> findRecentTransactions(Pageable pageable);
}
