package com.playtomic.tests.wallet.api.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@Data
@Entity
public class WalletTransaction {

    public WalletTransaction(BigDecimal oldBalance, BigDecimal newBalance) {
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
    }

    @Id
    @GeneratedValue(generator = "pg-uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "created_on", updatable = false)
    @CreationTimestamp
    private Instant createdOn;

    private BigDecimal oldBalance;

    private BigDecimal newBalance;

    @ManyToOne(fetch = FetchType.LAZY)
    private Wallet wallet;

}
