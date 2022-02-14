package com.playtomic.tests.wallet.api.service;

import com.playtomic.tests.wallet.api.entity.Wallet;
import com.playtomic.tests.wallet.api.entity.WalletTransaction;
import com.playtomic.tests.wallet.api.exceptions.EntityNotFoundException;
import com.playtomic.tests.wallet.api.repository.WalletRepository;
import com.playtomic.tests.wallet.api.response.WalletDto;
import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.StripeServiceException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * This is not the most fortunate approach: to use ExecutorService with Future to control Timeouts and Bulkhead
 * It is MUCH better to use, for example, Resilience4j library.
 * But, here is to show principle -> calling external service with transactional writes into database.
 */
@Slf4j
@Service
public class WalletService {

    private final int stripServiceTimeout;
    private final WalletRepository walletRepository;
    private final StripeService stripeService;
    private final ExecutorService executorService;

    public WalletService(@Value("${stripe.simulator.timeout-in-milliseconds}") int stripServiceTimeout,
                         @Value("${stripe.simulator.number-of-threads}") int numberOfThreads,
                         WalletRepository walletRepository,
                         StripeService stripeService) {
        this.stripServiceTimeout = stripServiceTimeout;
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
        this.walletRepository = walletRepository;
        this.stripeService = stripeService;
    }


    public Page<WalletDto> getAll(Pageable pageable) {
        Page<Wallet> wallets = walletRepository.findAll(pageable);
        return wallets.map(this::map);
    }

    public WalletDto getById(@NonNull UUID id) {
        Wallet wallet = getWalletById(id);
        return map(wallet);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WalletDto addToWallet(@NonNull UUID id, @NonNull BigDecimal amount) {

        Wallet wallet = getWalletById(id);
        WalletDto walletDto = add(wallet, amount);

        Future<?> future = executorService.submit(() -> stripeService.charge(wallet.getCreditCardNumber(), amount));

        try {
            future.get(stripServiceTimeout, TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            if ("com.playtomic.tests.wallet.service.StripeAmountTooSmallException".equals(e.getMessage())) {
                throw new StripeAmountTooSmallException();
            }
            throw new StripeServiceException();
        }

        return walletDto;
    }

    private WalletDto add(Wallet wallet, BigDecimal amount) {
        BigDecimal oldBalance = wallet.getBalance();
        BigDecimal newBalance = wallet.getBalance().add(amount);

        WalletTransaction transaction = new WalletTransaction(oldBalance, newBalance);

        wallet.setBalance(newBalance);
        wallet.addTransaction(transaction);

        Wallet savedWallet = walletRepository.save(wallet);
        return map(savedWallet);
    }

    private Wallet getWalletById(UUID id) {
        Optional<Wallet> optional = walletRepository.findById(id);
        return optional.orElseThrow(
                () -> new EntityNotFoundException("Entity with id=" + id + " is not Found"));
    }

    private WalletDto map(Wallet wallet) {
        return new WalletDto(
                wallet.getId(),
                wallet.getBalance(),
                wallet.getCreditCardNumber()
        );
    }


}
