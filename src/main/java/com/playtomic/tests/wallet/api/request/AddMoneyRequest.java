package com.playtomic.tests.wallet.api.request;

import lombok.Data;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
public class AddMoneyRequest {
    @Positive
    BigDecimal amount;
}
