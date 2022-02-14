package com.playtomic.tests.wallet.service.impl;


import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.StripeServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * This test is failing with the current implementation.
 *
 * How would you test this?
 */

@PropertySource("classpath:application.yml")
@RestClientTest(StripeService.class)
public class StripeServiceTest {

    @Autowired
    private StripeService s;

    @Autowired
    private MockRestServiceServer mockServer;

    @Value("${stripe.simulator.charges-uri}")
    private String chargesUri;

//    URI testUri = URI.create("http://how-would-you-test-me.localhost");
//    StripeService s = new StripeService(testUri, testUri, new RestTemplateBuilder());

    @Test
    public void test_exception() {

        this.mockServer.expect(requestTo(chargesUri))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        Assertions.assertThrows(StripeAmountTooSmallException.class,
                () -> s.charge("4242 4242 4242 4242", new BigDecimal(5)));
    }

    @Test
    public void test_ok() throws StripeServiceException {

        this.mockServer.expect(requestTo(chargesUri))
                .andRespond(withSuccess());

        s.charge("4242 4242 4242 4242", new BigDecimal(15));

    }
}
