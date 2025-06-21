package com.skyice.backend.controller;

import com.skyice.backend.dto.LoginRequest;
import com.skyice.backend.dto.RegisterRequest;
import com.skyice.backend.model.User;
import com.skyice.backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> register( @Valid @RequestBody RegisterRequest request) {
        System.out.println("Register request received for: " + request.getEmail());

        if (userRepo.existsByEmail(request.getEmail())) {
            System.out.println("User already exists: " + request.getEmail());
            return ResponseEntity.badRequest().body("User already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        System.out.println("Hashed password: " + hashedPassword);

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(hashedPassword)
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(request.getRole())
                .build();

        userRepo.save(user);
        System.out.println("User saved successfully: " + user.getEmail());
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {
        System.out.println("Login attempt for email: " + request.getEmail());

        Optional<User> userOptional = userRepo.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            System.out.println("User not found: " + request.getEmail());
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOptional.get();
        System.out.println("Stored hashed password: " + user.getPassword());
        System.out.println("Raw password: " + request.getPassword());

        boolean match = passwordEncoder.matches(request.getPassword(), user.getPassword());
        System.out.println("Password match result: " + match);

        if (match) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(401).body("Invalid credentials");
        }
    }
}