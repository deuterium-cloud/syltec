package com.playtomic.tests.wallet.api.controller;

import com.playtomic.tests.wallet.api.request.AddMoneyRequest;
import com.playtomic.tests.wallet.api.response.WalletDto;
import com.playtomic.tests.wallet.api.service.WalletServiceV2;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Slf4j
@RestController
@RequestMapping("/v2/wallets")
public class WalletControllerV2 {

    private final WalletServiceV2 walletService;
    private final Bulkhead bulkhead;

    public WalletControllerV2(WalletServiceV2 walletService,
                              @Value("${stripe.simulator.number-of-concurrent-calls}") int numberOfCalls) {
        this.walletService = walletService;
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(numberOfCalls)
                .build();

        BulkheadRegistry registry = BulkheadRegistry.of(config);
        this.bulkhead = registry.bulkhead("walletService");
    }

    @PostMapping("/{id}")
    public CompletableFuture<ResponseEntity<WalletDto>> addMoneyToWallet(@PathVariable UUID id, @Valid @RequestBody AddMoneyRequest request){

        log.info("POST request -> add amount={} to the Wallet with id={}", request.getAmount(), id);

        Supplier<WalletDto> walletSupplier = () -> walletService.addToWallet(id, request.getAmount());
        Supplier<WalletDto> decoratedWalletSupplier =
                Bulkhead.decorateSupplier(bulkhead, walletSupplier);

        return CompletableFuture
                .supplyAsync(decoratedWalletSupplier)
                .thenApply(ResponseEntity::ok);
    }

}
