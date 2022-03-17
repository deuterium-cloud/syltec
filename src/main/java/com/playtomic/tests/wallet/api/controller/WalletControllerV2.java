package com.playtomic.tests.wallet.api.controller;

import com.playtomic.tests.wallet.api.request.AddMoneyRequest;
import com.playtomic.tests.wallet.api.response.WalletDto;
import com.playtomic.tests.wallet.api.service.WalletServiceV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/v2/wallets")
public class WalletControllerV2 {

    private final WalletServiceV2 walletService;

    public WalletControllerV2(WalletServiceV2 walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/{id}")
    public ResponseEntity<WalletDto> addMoneyToWallet(@PathVariable UUID id, @Valid @RequestBody AddMoneyRequest request){

        log.info("POST request -> add amount={} to the Wallet with id={}", request.getAmount(), id);
        WalletDto walletDto = walletService.addToWallet(id, request.getAmount());

        return ResponseEntity.ok(walletDto);
    }

}
