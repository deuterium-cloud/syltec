package com.playtomic.tests.wallet.api.response;


import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Value
@Builder
public class WalletDto {
    UUID id;
    BigDecimal balance;
    String creditCardNumber;
}
