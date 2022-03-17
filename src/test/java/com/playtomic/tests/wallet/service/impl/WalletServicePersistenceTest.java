package com.playtomic.tests.wallet.service.impl;

import com.playtomic.tests.wallet.api.entity.Wallet;
import com.playtomic.tests.wallet.api.repository.WalletRepository;
import com.playtomic.tests.wallet.api.response.WalletDto;
import com.playtomic.tests.wallet.api.service.WalletService;
import com.playtomic.tests.wallet.service.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;

/**
 *  Testing Persistence layer
 */
@DataJpaTest
public class WalletServicePersistenceTest {

    @Autowired
    private WalletRepository walletRepository;

    @Mock
    private StripeService stripeService;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(1000, 2, walletRepository, stripeService);
    }

    @Sql("/wallets_01.sql")
    @Test
    @DisplayName("Should add amount to Wallet and save changes to database")
    void addToWallet() {
        UUID id = UUID.fromString("d4ebba32-bebb-42fc-9caf-e71023fb66e1");
        doNothing().when(stripeService).charge(anyString(), any(BigDecimal.class));

        WalletDto walletDto = walletService.addToWallet(id, new BigDecimal(200));

        assertEquals(new BigDecimal("300.00"), walletDto.getBalance());
        assertNotNull(walletDto.getId());
    }

    @Sql("/wallets_02.sql")
    @Test
    @DisplayName("Should add amount to Wallet and create Transaction")
    void addToWallet_checkTransaction() {
        UUID id = UUID.fromString("d4ebba32-bebb-42fc-9caf-e71023fb66e2");
        doNothing().when(stripeService).charge(anyString(), any(BigDecimal.class));

        WalletDto walletDto = walletService.addToWallet(id, new BigDecimal(200));

        assertEquals(new BigDecimal("300.00"), walletDto.getBalance());

        Optional<Wallet> optional = walletRepository.findById(id);
        assertEquals(1, optional.get().getTransactions().size());
        assertEquals(new BigDecimal("100.00"), optional.get().getTransactions().get(0).getOldBalance());
        assertEquals(new BigDecimal("300.00"), optional.get().getTransactions().get(0).getNewBalance());
    }

}
