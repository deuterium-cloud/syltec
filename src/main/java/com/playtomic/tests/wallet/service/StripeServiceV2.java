package com.playtomic.tests.wallet.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;

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
                           @Value("${stripe.simulator.refunds-uri}") @NonNull URI refundsUri,
                           @Value("${stripe.simulator.timeout-in-milliseconds}") @NonNull int timeout) {
        this.chargesUri = chargesUri;
        this.refundsUri = refundsUri;
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(timeout));
        this.client = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bulkhead(name = "stripeService")
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

    @Bulkhead(name = "stripeService")
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
