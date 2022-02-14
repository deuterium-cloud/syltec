package com.playtomic.tests.wallet.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;

/**
 * Handles the communication with Stripe using Spring Webclient.
 * Here in the blocking mode, only to follow the given design paradigm.
 * Should be in non-blocking mode.
 */

@Slf4j
@Service
public class StripeServiceV2 {

    @NonNull
    private URI chargesUri;

    @NonNull
    private URI refundsUri;

    @NonNull
    private WebClient client;

    public StripeServiceV2(@Value("${stripe.simulator.charges-uri}") @NonNull URI chargesUri,
                           @Value("${stripe.simulator.refunds-uri}") @NonNull URI refundsUri) {
        this.chargesUri = chargesUri;
        this.refundsUri = refundsUri;
        this.client = WebClient.create();
    }

    public void charge(@NonNull String creditCardNumber, @NonNull BigDecimal amount) throws StripeServiceException {
        ChargeRequestV2 body = new ChargeRequestV2(creditCardNumber, amount);
        client.post()
                .uri(chargesUri)
                .body(Mono.just(body), ChargeRequestV2.class)
                .retrieve()
                .onStatus(status -> status.value() == 422, res -> Mono.error(new StripeAmountTooSmallException()))
                .bodyToMono(Object.class)
                .block();
    }

    public void refund(@NonNull String paymentId) throws StripeServiceException {

        client.post()
                .uri(uriBuilder -> uriBuilder
                        .path(refundsUri.toString())
                        .build(paymentId))
                .retrieve()
                .onStatus(HttpStatus::isError, res -> Mono.error(new StripeServiceException()))
                .bodyToMono(Object.class)
                .block();
    }

    @AllArgsConstructor
    private static class ChargeRequestV2 {

        @NonNull
        @JsonProperty("credit_card")
        String creditCardNumber;

        @NonNull
        @JsonProperty("amount")
        BigDecimal amount;
    }
}
