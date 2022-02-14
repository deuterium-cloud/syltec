package com.playtomic.tests.wallet.api.controller;

import com.playtomic.tests.wallet.api.request.AddMoneyRequest;
import com.playtomic.tests.wallet.api.response.WalletDto;
import com.playtomic.tests.wallet.api.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<Page<WalletDto>> getWallets(@PageableDefault Pageable pageable){
        log.info("GET request -> get all Wallets");
        Page<WalletDto> wallets = walletService.getAll(pageable);
        return ResponseEntity.ok(wallets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletDto> getWalletById(@PathVariable UUID id){
        log.info("GET request -> get the Wallet with id={}", id);
        WalletDto wallet = walletService.getById(id);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/{id}")
    public ResponseEntity<WalletDto> addMoneyToWallet(@PathVariable UUID id, @Valid @RequestBody AddMoneyRequest request){
        log.info("POST request -> add amount={} to the Wallet with id={}", request.getAmount(), id);
        WalletDto wallet = walletService.addToWallet(id, request.getAmount());
        return ResponseEntity.ok(wallet);
    }

}
