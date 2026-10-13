package com.ds.securecampus.service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_MINUTES = 15;

    private static class AttemptInfo {
        int attempts;
        LocalDateTime lockExpiry;

        AttemptInfo(int attempts) {
            this.attempts = attempts;
        }
    }

    private final ConcurrentHashMap<String, AttemptInfo> attemptsCache = new ConcurrentHashMap<>();

    public void loginFailed(String key) {
        if (key == null || key.isBlank()) return;
        attemptsCache.compute(key, (k, info) -> {
            if (info == null) {
                return new AttemptInfo(1);
            }
            if (info.lockExpiry != null && LocalDateTime.now().isAfter(info.lockExpiry)) {
                // El bloqueo expiró, reiniciar
                return new AttemptInfo(1);
            }
            info.attempts++;
            if (info.attempts >= MAX_ATTEMPTS) {
                info.lockExpiry = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
            }
            return info;
        });
    }

    public void loginSucceeded(String key) {
        if (key != null) {
            attemptsCache.remove(key);
        }
    }

    public boolean isBlocked(String key) {
        if (key == null) return false;
        AttemptInfo info = attemptsCache.get(key);
        if (info == null || info.lockExpiry == null) {
            return false;
        }
        if (LocalDateTime.now().isBefore(info.lockExpiry)) {
            return true;
        }
        // Expiró el bloqueo
        attemptsCache.remove(key);
        return false;
    }

    public int getRemainingLockMinutes(String key) {
        AttemptInfo info = attemptsCache.get(key);
        if (info == null || info.lockExpiry == null) return 0;
        long seconds = java.time.Duration.between(LocalDateTime.now(), info.lockExpiry).getSeconds();
        return (int) Math.max(1, (seconds + 59) / 60);
    }
}
