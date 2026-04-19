package com.indore.service.impl;

import com.indore.dto.UpdatePasswordRequest;
import com.indore.dto.UpdateProfileRequest;
import com.indore.entity.User;
import com.indore.repository.UserRepository;
import com.indore.service.AuditLogService;
import com.indore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()->new RuntimeException("User not found"));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);

    }

    @Override
    public void updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        userRepository.save(user);

        auditLogService.log(email, "PROFILE_UPDATED", "Account", "Name updated to: " + request.getName(), "SUCCESS", email);
    }

    @Override
    public void updatePassword(String email, UpdatePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            auditLogService.log(email, "PASSWORD_CHANGED", "Security", "Failed password change attempt", "FAILED", email);
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        auditLogService.log(email, "PASSWORD_CHANGED", "Security", "Password updated successfully", "SUCCESS", email);
    }

    @Override
    public void updateProfileImage(String email, String imageUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setProfileImage(imageUrl);
        userRepository.save(user);
        auditLogService.log(email, "AVATAR_UPDATED", "Account", "Profile image updated", "SUCCESS", email);
    }
}
