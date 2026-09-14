package com.robustrade.wallet.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.robustrade.wallet.handler.dto.CreateWalletRequest;
import com.robustrade.wallet.handler.dto.WalletResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class WalletApiIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createWallet_persistsAndReturnsCreatedWallet() throws Exception {
        WalletResponse response = createWallet(1001L, "1000.00");

        assertThat(response.getWalletId()).isNotBlank();
        assertThat(response.getUserId()).isEqualTo(1001L);
        assertThat(response.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(walletBalance(response.getWalletId())).isEqualByComparingTo("1000.00");
    }

    @Test
    void createWallet_whenUserAlreadyHasWallet_returnsConflict() throws Exception {
        createWallet(1001L, "100.00");

        CreateWalletRequest duplicate = CreateWalletRequest.builder()
                .userId(1001L)
                .balance(new BigDecimal("50.00"))
                .build();

        mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("WALLET_ALREADY_EXISTS"));
    }

    @Test
    void createWallet_whenBalanceNegative_returnsBadRequest() throws Exception {
        CreateWalletRequest request = CreateWalletRequest.builder()
                .userId(1002L)
                .balance(new BigDecimal("-1.00"))
                .build();

        mockMvc.perform(post("/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
}
