package com.kammo.kammobackend.wallet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.Check;

/**
 * One per {@link com.kammo.kammobackend.user.AppUser}. The {@code balance} is an internal
 * accounting figure representing credits Kammo is tracking on the user's behalf — it is NOT
 * money Kammo physically holds. Real funds always sit in Paystack/Ozow's merchant balance until
 * a payout transfer moves them to the user's own bank account.
 */
@Entity
@Table(name = "wallet_accounts")
@Check(name = "wallet_balance_non_negative", constraints = "balance >= 0")
public class WalletAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    @Version
    private Long version;

    protected WalletAccount() {
    }

    public WalletAccount(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    /**
     * Increases the wallet's credit balance. Used for HOLD entries.
     */
    void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.updatedAt = Instant.now();
    }

    /**
     * Decreases the wallet's credit balance. Used for PAYOUT, REFUND and REVERSAL entries.
     * Never allows the balance to drop below zero.
     */
    void debit(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                "Wallet balance cannot go negative: balance=" + this.balance + ", debit=" + amount
            );
        }
        this.balance = this.balance.subtract(amount);
        this.updatedAt = Instant.now();
    }
}
