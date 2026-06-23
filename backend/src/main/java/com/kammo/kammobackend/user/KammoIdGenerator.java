package com.kammo.kammobackend.user;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class KammoIdGenerator {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int KAMMO_ID_LENGTH = 8;
    private static final int MAX_ATTEMPTS = 20;

    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;

    public KammoIdGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateUniqueKammoId() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String kammoId = randomId();
            if (!userRepository.existsByKammoId(kammoId)) {
                return kammoId;
            }
        }

        throw new IllegalStateException("Could not allocate a unique Kammo ID");
    }

    private String randomId() {
        StringBuilder id = new StringBuilder(KAMMO_ID_LENGTH);
        for (int index = 0; index < KAMMO_ID_LENGTH; index++) {
            id.append(ALPHABET.charAt(secureRandom.nextInt(ALPHABET.length())));
        }
        return id.toString();
    }
}
