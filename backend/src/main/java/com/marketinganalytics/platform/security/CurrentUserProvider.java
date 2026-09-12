package com.marketinganalytics.platform.security;

import com.marketinganalytics.platform.entity.User;
import com.marketinganalytics.platform.exception.ResourceNotFoundException;
import com.marketinganalytics.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityUser securityUser)) {
            throw new ResourceNotFoundException("No hay un usuario autenticado en el contexto actual");
        }
        return userRepository.findById(securityUser.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("Usuario", securityUser.getId()));
    }
}
