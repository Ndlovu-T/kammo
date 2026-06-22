package com.kammo.kammobackend.auth;

import com.kammo.kammobackend.user.AppUser;
import com.kammo.kammobackend.user.KammoIdGenerator;
import com.kammo.kammobackend.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final KammoIdGenerator kammoIdGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        KammoIdGenerator kammoIdGenerator,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.kammoIdGenerator = kammoIdGenerator;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new IllegalArgumentException("Phone number is already registered");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        AppUser user = new AppUser(
            kammoIdGenerator.generateUniqueKammoId(),
            request.phoneNumber(),
            request.email(),
            passwordEncoder.encode(request.password())
        );

        AppUser savedUser = userRepository.save(user);
        return toAuthResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.phoneNumber(), request.password())
        );

        AppUser user = userRepository.findByPhoneNumber(request.phoneNumber())
            .orElseThrow(() -> new IllegalArgumentException("Invalid phone number or password"));

        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(AppUser user) {
        return new AuthResponse(
            jwtService.generateToken(user),
            user.getKammoId(),
            user.getPhoneNumber(),
            user.getEmail()
        );
    }
}
