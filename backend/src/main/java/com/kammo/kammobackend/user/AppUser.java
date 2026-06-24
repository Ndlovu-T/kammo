package com.kammo.kammobackend.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Table(name = "users")
public class AppUser implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kammo_id", nullable = false, unique = true, length = 8)
    private String kammoId;

    @Column(nullable = false, unique = true, length = 30)
    private String phoneNumber;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(length = 10)
    private String bankCode;

    @Column(length = 20)
    private String bankAccountNumber;

    @Column(length = 100)
    private String bankAccountName;

    @Column(length = 50)
    private String paystackRecipientCode;

    protected AppUser() {
    }

    public AppUser(String kammoId, String phoneNumber, String email, String password) {
        this.kammoId = kammoId;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getKammoId() {
        return kammoId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Role getRole() {
        return role;
    }

    public void promoteToAdmin() {
        this.role = Role.ADMIN;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public boolean hasBankAccount() {
        return bankCode != null && bankAccountNumber != null;
    }

    public void updateBankAccount(String bankCode, String bankAccountNumber, String bankAccountName) {
        this.bankCode = bankCode;
        this.bankAccountNumber = bankAccountNumber;
        this.bankAccountName = bankAccountName;
        this.paystackRecipientCode = null;
    }

    public String getPaystackRecipientCode() {
        return paystackRecipientCode;
    }

    public void setPaystackRecipientCode(String paystackRecipientCode) {
        this.paystackRecipientCode = paystackRecipientCode;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return phoneNumber;
    }
}
