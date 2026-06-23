package com.kammo.kammobackend.deal;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class DealCodeGenerator {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 20;

    private final SecureRandom secureRandom = new SecureRandom();
    private final DealRepository dealRepository;

    public DealCodeGenerator(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    public String generateUniqueDealCode() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = randomCode();
            if (!dealRepository.existsByDealCode(code)) {
                return code;
            }
        }

        throw new IllegalStateException("Could not allocate a unique deal code");
    }

    private String randomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int index = 0; index < CODE_LENGTH; index++) {
            code.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
