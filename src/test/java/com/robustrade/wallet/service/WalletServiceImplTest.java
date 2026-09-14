package com.robustrade.wallet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.robustrade.wallet.domain.Wallet;
import com.robustrade.wallet.handler.dto.CreateWalletRequest;
import com.robustrade.wallet.handler.dto.WalletResponse;
import com.robustrade.wallet.repository.WalletRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    void createWallet_whenUserHasNoWallet_savesAndReturnsResponse() {
        CreateWalletRequest request = CreateWalletRequest.builder()
                .userId(1001L)
                .balance(new BigDecimal("1000.00"))
                .build();

        when(walletRepository.existsByUserId(1001L)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = walletService.createWallet(request);

        assertThat(response.getUserId()).isEqualTo(1001L);
        assertThat(response.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(response.getWalletId()).isNotBlank();
        assertThat(response.getCreatedAt()).isNotNull();

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).existsByUserId(1001L);
        verify(walletRepository).save(walletCaptor.capture());

        Wallet saved = walletCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1001L);
        assertThat(saved.getBalance()).isEqualByComparingTo("1000.00");
        assertThat(saved.getId()).isEqualTo(response.getWalletId());
    }

    @Test
    void createWallet_whenWalletAlreadyExists_throwsConflict() {
        CreateWalletRequest request = CreateWalletRequest.builder()
                .userId(1001L)
                .balance(new BigDecimal("500.00"))
                .build();

        when(walletRepository.existsByUserId(1001L)).thenReturn(true);

        assertThatThrownBy(() -> walletService.createWallet(request))
                .isInstanceOf(WalletServiceException.class)
                .satisfies(ex -> {
                    WalletServiceException serviceException = (WalletServiceException) ex;
                    assertThat(serviceException.getErrorCode()).isEqualTo("WALLET_ALREADY_EXISTS");
                    assertThat(serviceException.getHttpStatus()).isEqualTo(409);
                    assertThat(serviceException.getMessage()).contains("1001");
                });

        verify(walletRepository).existsByUserId(1001L);
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}
