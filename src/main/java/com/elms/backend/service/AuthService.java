package com.elms.backend.service;

import com.elms.backend.dto.AuthResponse;
import com.elms.backend.dto.LoginRequest;
import com.elms.backend.dto.RegisterRequest;
import com.elms.backend.entity.Role;
import com.elms.backend.entity.User;
import com.elms.backend.repository.UserRepository;
import com.elms.backend.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setRole(Role.EMPLOYEE);

        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        return new AuthResponse(
                savedUser.getId(),
                token,
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"
                        )
                );

        String storedPassword = user.getPassword();

        if (storedPassword != null &&
                storedPassword.startsWith("$2")) {

            if (!passwordEncoder.matches(
                    request.getPassword(),
                    storedPassword)) {

                throw new RuntimeException(
                        "Invalid email or password"
                );
            }

        } else {

            if (!request.getPassword()
                    .equals(storedPassword)) {

                throw new RuntimeException(
                        "Invalid email or password"
                );
            }

            user.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );

            userRepository.save(user);
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        return new AuthResponse(
                user.getId(),
                token,
                user.getFullName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}