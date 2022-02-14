package com.playtomic.tests.wallet.config;

import com.playtomic.tests.wallet.api.entity.Wallet;
import com.playtomic.tests.wallet.api.repository.WalletRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Profile("develop")
@Slf4j
@AllArgsConstructor
@Component
public class InsertInitialData implements CommandLineRunner {

    private final WalletRepository walletRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Inserting initial Wallets into Database...");

        List<Wallet> wallets = Stream.of(
                        new Wallet(null, null, null,
                                new BigDecimal(100), "1111 2222 3333 4444", new ArrayList<>()),
                        new Wallet(null, null, null,
                                new BigDecimal(500), "5555 6666 7777 8888", new ArrayList<>()))
                .collect(Collectors.toList());

        walletRepository.saveAll(wallets);
        log.info("Wallets successfully inserted");
    }
}
