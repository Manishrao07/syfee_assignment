package com.syfe.finance_manager.controller;

import com.syfe.finance_manager.dto.LoginRequest;
import com.syfe.finance_manager.dto.MessageResponse;
import com.syfe.finance_manager.dto.UserRegistrationRequest;
import com.syfe.finance_manager.dto.UserRegistrationResponse;
import com.syfe.finance_manager.entity.User;
import com.syfe.finance_manager.exception.ConflictException;
import com.syfe.finance_manager.exception.UnauthorizedException;
import com.syfe.finance_manager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(UserRepository userRepository, 
                          PasswordEncoder passwordEncoder, 
                          AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(@Valid @RequestBody UserRegistrationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username/email already exists");
        }

        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                request.getPhoneNumber()
        );

        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UserRegistrationResponse("User registered successfully", savedUser.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponse> login(@Valid @RequestBody LoginRequest request, 
                                                 HttpServletRequest httpRequest, 
                                                 HttpServletResponse httpResponse) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            
            // Save context in security repository to support session-based auth across requests
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            return ResponseEntity.ok(new MessageResponse("Login successful"));
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Invalid username or password");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("Logout successful"));
    }

    // Overloaded helper for response serialization
    private static class LogoutResponse {
        private String message;
        public LogoutResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}
