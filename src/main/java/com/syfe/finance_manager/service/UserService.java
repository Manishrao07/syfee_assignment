package com.syfe.finance_manager.service;

import com.syfe.finance_manager.entity.User;
import com.syfe.finance_manager.exception.UnauthorizedException;
import com.syfe.finance_manager.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new UnauthorizedException("User is not authenticated");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }
}
