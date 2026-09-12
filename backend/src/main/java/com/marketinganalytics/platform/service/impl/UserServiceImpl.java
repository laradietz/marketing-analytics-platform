package com.marketinganalytics.platform.service.impl;

import com.marketinganalytics.platform.dto.user.ChangePasswordRequest;
import com.marketinganalytics.platform.dto.user.UpdateProfileRequest;
import com.marketinganalytics.platform.dto.user.UserResponse;
import com.marketinganalytics.platform.entity.User;
import com.marketinganalytics.platform.exception.BadRequestException;
import com.marketinganalytics.platform.mapper.UserMapper;
import com.marketinganalytics.platform.repository.UserRepository;
import com.marketinganalytics.platform.security.CurrentUserProvider;
import com.marketinganalytics.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentProfile() {
        return UserMapper.toResponse(currentUserProvider.getCurrentUser());
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        User user = currentUserProvider.getCurrentUser();
        user.setName(request.name());
        user.setJobTitle(request.jobTitle());
        user.setCompanyName(request.companyName());
        return UserMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = currentUserProvider.getCurrentUser();
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("La contraseña actual no es correcta");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
