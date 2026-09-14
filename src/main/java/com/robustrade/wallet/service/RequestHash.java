package com.robustrade.wallet.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Lightweight stable hash of transfer payload fields for idempotency conflict detection.
 * Uses SHA-256 hex (64 chars) over {@code fromWalletId|toWalletId|amount}.
 */
public final class RequestHash {

    private RequestHash() {
    }

    public static String from(String fromWalletId, String toWalletId, BigDecimal amount) {
        String payload = fromWalletId + "|" + toWalletId + "|" + amount.toPlainString();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
