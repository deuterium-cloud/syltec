package com.playtomic.tests.wallet;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.playtomic.tests.wallet.api.entity.Wallet;
import com.playtomic.tests.wallet.api.repository.WalletRepository;
import com.playtomic.tests.wallet.service.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.service.StripeService;
import com.playtomic.tests.wallet.service.StripeServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WireMockTest(httpPort = 9999)
@AutoConfigureMockMvc
@SpringBootTest
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class WalletApplicationIT {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	private WalletRepository walletRepository;

	@Autowired
	private StripeService s;

//	@Autowired
//	WebTestClient webTestClient;


	@Test
	public void test_exception() {
		stubFor(post("/").willReturn(status(422)));
		Assertions.assertThrows(StripeAmountTooSmallException.class,
				() -> s.charge("4242 4242 4242 4242", new BigDecimal(5)));
	}

	@Test
	public void test_ok() throws StripeServiceException {
		stubFor(post("/").willReturn(ok()));
		s.charge("4242 4242 4242 4242", new BigDecimal(15));
	}

	@Sql("/wallets_01.sql")
	@Test
	@DisplayName("Should return WalletDto with provided identifier")
	public void getWalletById() throws Exception {

		this.mockMvc.perform(get("/v1/wallets/d4ebba32-bebb-42fc-9caf-e71023fb66e1"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
				.andExpect(jsonPath("$.id").value("d4ebba32-bebb-42fc-9caf-e71023fb66e1"))
				.andExpect(jsonPath("$.balance").value(100))
				.andExpect(jsonPath("$.creditCardNumber").value("1111 2222 3333 4444"));

	}

	@Sql("/wallets_02.sql")
	@Test
	@DisplayName("Should update Wallet with given amount of money")
	public void addMoneyToWallet() throws Exception {
		UUID id = UUID.fromString("d4ebba32-bebb-42fc-9caf-e71023fb66e2");

		stubFor(post("/").willReturn(ok()));

		String json = "{ \"amount\": 300 }";

		this.mockMvc.perform(MockMvcRequestBuilders.post("/v1/wallets/d4ebba32-bebb-42fc-9caf-e71023fb66e2")
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(json))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

		Optional<Wallet> optional = walletRepository.findById(id);
		assertEquals(new BigDecimal("400.00"), optional.get().getBalance());
	}

	@Sql("/wallets_03.sql")
	@Test
	@DisplayName("Should return status 400 because amount is less then minimal, and amount in the database should not changed")
	public void addMoneyToWallet_withLessAmount() throws Exception {
		UUID id = UUID.fromString("d4ebba32-bebb-42fc-9caf-e71023fb66e3");

		stubFor(post("/").willReturn(status(422)));

		String json = "{ \"amount\": 5 }";

		this.mockMvc.perform(MockMvcRequestBuilders.post("/v1/wallets/d4ebba32-bebb-42fc-9caf-e71023fb66e3")
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(json))
				.andExpect(MockMvcResultMatchers.status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

		Optional<Wallet> optional = walletRepository.findById(id);
		assertEquals(new BigDecimal("100.00"), optional.get().getBalance());
	}

	@Sql("/wallets_04.sql")
	@Test
	@DisplayName("Should return status 500 because external service did not respond in given period of time." +
			"Amount in the Wallet in the database, should not be changed")
	public void addMoneyToWallet_withTimeoutExceeded() throws Exception {
		UUID id = UUID.fromString("d4ebba32-bebb-42fc-9caf-e71023fb66e4");

		stubFor(post("/").willReturn(aResponse().withFixedDelay(3000)));

		String json = "{ \"amount\": 300 }";

		this.mockMvc.perform(MockMvcRequestBuilders.post("/v1/wallets/d4ebba32-bebb-42fc-9caf-e71023fb66e4")
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(json))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

		Optional<Wallet> optional = walletRepository.findById(id);
		assertEquals(new BigDecimal("100.00"), optional.get().getBalance());
	}


	@Sql("/wallets_05.sql")
	@Test
	@DisplayName("Should return status 500 because external service is unavailable." +
			"Amount in the Wallet in the database, should not be changed")
	public void addMoneyToWallet_unavailableExternalService() throws Exception {
		UUID id = UUID.fromString("d4ebba32-bebb-42fc-9caf-e71023fb66e5");

		stubFor(post("/").willReturn(serviceUnavailable()));

		String json = "{ \"amount\": 300 }";

		this.mockMvc.perform(MockMvcRequestBuilders.post("/v1/wallets/d4ebba32-bebb-42fc-9caf-e71023fb66e5")
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.content(json))
				.andExpect(MockMvcResultMatchers.status().isInternalServerError())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

		Optional<Wallet> optional = walletRepository.findById(id);
		assertEquals(new BigDecimal("100.00"), optional.get().getBalance());

	}

}
