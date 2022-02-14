package com.playtomic.tests.wallet.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Wallet {

    @Id
    @GeneratedValue(generator = "pg-uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "created_on", updatable = false)
    @CreationTimestamp
    private Instant createdOn;

    @Column(name = "updated_on", updatable = true)
    @UpdateTimestamp
    private Instant updatedOn;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "credit_card_number")
    private String creditCardNumber;

    @OneToMany(
            mappedBy = "wallet",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = false
    )
    private List<WalletTransaction> transactions = new ArrayList<>();

    public void addTransaction(WalletTransaction transaction) {
        transactions.add(transaction);
        transaction.setWallet(this);
    }
}
