package com.robustrade.wallet.service;

import com.robustrade.wallet.domain.Wallet;
import com.robustrade.wallet.handler.dto.CreateWalletRequest;
import com.robustrade.wallet.handler.dto.WalletResponse;
import com.robustrade.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        log.info(
                "Creating wallet for userId={} with initial balance={}",
                request.getUserId(),
                request.getBalance()
        );
        log.debug("Persisting new wallet entity for userId={}", request.getUserId());

        try {
            if (walletRepository.existsByUserId(request.getUserId())) {
                log.error("Wallet already exists for userId={}", request.getUserId());
                throw WalletServiceException.walletAlreadyExists(request.getUserId());
            }

            Wallet wallet = Wallet.create(request.getUserId(), request.getBalance());
            walletRepository.save(wallet);

            log.info(
                    "Wallet persisted successfully: walletId={}, userId={}, balance={}",
                    wallet.getId(),
                    wallet.getUserId(),
                    wallet.getBalance()
            );
            log.debug(
                    "Wallet entity details: id={}, userId={}, balance={}, createdAt={}",
                    wallet.getId(),
                    wallet.getUserId(),
                    wallet.getBalance(),
                    wallet.getCreatedAt()
            );

            return WalletResponse.from(wallet);
        } catch (Exception ex) {
            log.error(
                    "Error while creating wallet for userId={}: {}",
                    request.getUserId(),
                    ex.getMessage(),
                    ex
            );
            throw ex;
        }
    }
}
