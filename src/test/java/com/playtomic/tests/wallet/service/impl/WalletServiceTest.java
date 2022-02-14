package com.playtomic.tests.wallet.service.impl;

import com.playtomic.tests.wallet.api.entity.Wallet;
import com.playtomic.tests.wallet.api.repository.WalletRepository;
import com.playtomic.tests.wallet.api.response.WalletDto;
import com.playtomic.tests.wallet.api.service.WalletService;
import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.StripeServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static com.playtomic.tests.wallet.utils.Stubber.doSleep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private StripeService stripeService;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(1000, 2, walletRepository, stripeService);
    }

    @DisplayName("Should add given amount of money to the wallet with given identifier")
    @Test
    void addToWallet() {
        UUID id = UUID.fromString("d4ebba32-bebb-42fc-9caf-e71023fb66ef");
        Wallet wallet = createWallet();

        when(walletRepository.findById(any(UUID.class))).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        doNothing().when(stripeService).charge(anyString(), any(BigDecimal.class));


        WalletDto walletDto = walletService.addToWallet(id, new BigDecimal(100));


        assertEquals(new BigDecimal(100 + 500), walletDto.getBalance());

        verify(walletRepository, times(1)).findById(id);

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(1)).save(captor.capture());

        assertEquals(new BigDecimal(100 + 500), captor.getValue().getBalance());

        verify(stripeService, times(1))
                .charge("1111 2222 3333 4444", new BigDecimal(100));

    }

    @DisplayName("Should throw the StripeServiceException exception when StripeService didn't response after defined time interval")
    @Test
    void addToWallet_withException_01() {
        UUID id = UUID.fromString("d4ebba32-bebb-42fc-9caf-e71023fb66ef");
        Wallet wallet = createWallet();

        when(walletRepository.findById(any(UUID.class))).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(new Wallet());

        doSleep(Duration.ofSeconds(3)).when(stripeService).charge(anyString(), any(BigDecimal.class));

        assertThrows(StripeServiceException.class,
                () -> walletService.addToWallet(id, new BigDecimal(100)));
    }

    @DisplayName("Should throw the StripeServiceException exception when StripeService throws StripeAmountTooSmallException")
    @Test
    void addToWallet_withException_02() {
        UUID id = UUID.fromString("d4ebba32-bebb-42fc-9caf-e71023fb66ef");
        Wallet wallet = createWallet();

        when(walletRepository.findById(any(UUID.class))).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(new Wallet());
        doThrow(new StripeAmountTooSmallException())
                .when(stripeService).charge(anyString(), any(BigDecimal.class));

        assertThrows(StripeServiceException.class,
                () -> walletService.addToWallet(id, new BigDecimal(100)));
    }

    @DisplayName("Should NOT call StripeService when writing in Database throws any Exception")
    @Test
    void addToWallet_withException_03() {
        Wallet wallet = createWallet();

        when(walletRepository.findById(any(UUID.class))).thenReturn(Optional.of(wallet));

        doThrow(new RuntimeException())
                .when(walletRepository).save(any(Wallet.class));

        verify(stripeService, times(0))
                .charge(anyString(), any(BigDecimal.class));
    }

    private Wallet createWallet() {
        UUID id = UUID.fromString("d4ebba32-bebb-42fc-9caf-e71023fb66ef");
        String creditCardNumber = "1111 2222 3333 4444";
        Instant createdAt = Instant.parse("2021-12-29T09:25:50.820Z");
        Instant updatedAt = Instant.parse("2022-12-29T09:25:50.820Z");
        BigDecimal amount = new BigDecimal(500);
        return new Wallet(id, createdAt, updatedAt, amount, creditCardNumber, new ArrayList<>());
    }
}
