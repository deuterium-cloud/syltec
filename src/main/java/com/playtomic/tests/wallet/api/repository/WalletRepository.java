package com.playtomic.tests.wallet.api.repository;

import com.playtomic.tests.wallet.api.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
}
