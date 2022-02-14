package com.playtomic.tests.wallet.controller;

import com.playtomic.tests.wallet.api.controller.WalletController;
import com.playtomic.tests.wallet.api.response.WalletDto;
import com.playtomic.tests.wallet.api.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
public class WalletControllerTest {

    private MockMvc mockMvc;

    @Mock
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new WalletController(walletService))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("Should return WalletDto with provided identifier")
    void getWalletById() throws Exception {
        WalletDto walletDto = createWalletDto();
        when(walletService.getById(any(UUID.class))).thenReturn(walletDto);

        this.mockMvc.perform(get("/v1/wallets/d4ebba32-bebb-42fc-9caf-e71023fb66ef"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value("d4ebba32-bebb-42fc-9caf-e71023fb66ef"))
                .andExpect(jsonPath("$.balance").value(500))
                .andExpect(jsonPath("$.creditCardNumber").value("1111 2222 3333 4444"));

        verify(walletService, times(1))
                .getById(UUID.fromString("d4ebba32-bebb-42fc-9caf-e71023fb66ef"));
    }

    @Test
    @DisplayName("Should return a page of WalletDtos")
    void getWallets() throws Exception {
        WalletDto walletDto = createWalletDto();
        Page<WalletDto> page = new PageImpl<>(Collections.singletonList(walletDto), Pageable.unpaged(), 0);
        when(walletService.getAll(any(Pageable.class))).thenReturn(page);

        this.mockMvc.perform(get("/v1/wallets")
                        .param("page", "2")
                        .param("size", "25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.content[0].id").value("d4ebba32-bebb-42fc-9caf-e71023fb66ef"))
                .andExpect(jsonPath("$.content[0].balance").value(500))
                .andExpect(jsonPath("$.content[0].creditCardNumber").value("1111 2222 3333 4444"));

        ArgumentCaptor<Pageable> pageCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(walletService, times(1)).getAll(pageCaptor.capture());

        PageRequest pageable = (PageRequest) pageCaptor.getValue();
        assertEquals(2, pageable.getPageNumber());
        assertEquals(25, pageable.getPageSize());
    }

    @Test
    @DisplayName("Should call WalletService to update Wallet with given amount of money")
    void addMoneyToWallet() throws Exception {
        WalletDto walletDto = createWalletDto();
        String json = "{ \"amount\": 300 }";

        when(walletService.addToWallet(any(UUID.class), any(BigDecimal.class))).thenReturn(walletDto);

        this.mockMvc.perform(post("/v1/wallets/d4ebba32-bebb-42fc-9caf-e71023fb66ef")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

        verify(walletService, times(1))
                .addToWallet(UUID.fromString("d4ebba32-bebb-42fc-9caf-e71023fb66ef"), new BigDecimal(300));
    }

    @Test
    @DisplayName("Should return 400 Bad request when amount has some negative value")
    void addMoneyToWallet_withNegativeValue() throws Exception {

        String json = "{ \"amount\":-300 }";

        this.mockMvc.perform(post("/v1/wallets/d4ebba32-bebb-42fc-9caf-e71023fb66ef")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    private WalletDto createWalletDto() {
        UUID id = UUID.fromString("d4ebba32-bebb-42fc-9caf-e71023fb66ef");
        String creditCardNumber = "1111 2222 3333 4444";
        BigDecimal balance = new BigDecimal(500);
        return new WalletDto(id, balance, creditCardNumber);
    }
}
