package com.playtomic.tests.wallet.api.service;

import com.playtomic.tests.wallet.api.entity.Wallet;
import com.playtomic.tests.wallet.api.entity.WalletTransaction;
import com.playtomic.tests.wallet.api.exceptions.EntityNotFoundException;
import com.playtomic.tests.wallet.api.repository.WalletRepository;
import com.playtomic.tests.wallet.api.response.WalletDto;
import com.playtomic.tests.wallet.service.StripeServiceV2;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class WalletServiceV2 {

    private final WalletRepository walletRepository;
    private final StripeServiceV2 stripeService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public WalletDto addToWallet(@NonNull UUID id, @NonNull BigDecimal amount) {

        Wallet wallet = getWalletById(id);
        WalletDto walletDto = add(wallet, amount);

        stripeService.charge(wallet.getCreditCardNumber(), amount);

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
