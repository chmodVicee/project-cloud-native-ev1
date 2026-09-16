package com.cloudnative.backend.service;

import com.cloudnative.backend.dto.AuthResponse;
import com.cloudnative.backend.dto.LoginRequest;
import com.cloudnative.backend.dto.RegisterRequest;
import com.cloudnative.backend.exception.UserAlreadyExistException;
import com.cloudnative.backend.model.Role;
import com.cloudnative.backend.model.User;
import com.cloudnative.backend.repository.UserRepository;
import com.cloudnative.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register (RegisterRequest request){

        if (userRepository.existsByUsername(request.getUsername())){
            throw new UserAlreadyExistException("Ya se ha utilizado el username de: " + request.getUsername());
        }

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistException("Ya se ha utilizado el email de: " + request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new AuthResponse(token);
    }

    public AuthResponse login (LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()
                )
        );

        User user = userRepository.findByUsername (request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
