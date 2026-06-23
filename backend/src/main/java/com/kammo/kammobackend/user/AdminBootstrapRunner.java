package com.kammo.kammobackend.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserRepository userRepository;

    @Value("${app.admin.phone-number:}")
    private String adminPhoneNumber;

    public AdminBootstrapRunner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminPhoneNumber == null || adminPhoneNumber.isBlank()) {
            return;
        }

        userRepository.findByPhoneNumber(adminPhoneNumber).ifPresent(user -> {
            if (user.getRole() != Role.ADMIN) {
                user.promoteToAdmin();
                userRepository.save(user);
            }
        });
    }
}
