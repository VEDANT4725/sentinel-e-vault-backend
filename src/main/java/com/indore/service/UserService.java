package com.indore.service;

import com.indore.dto.UpdatePasswordRequest;
import com.indore.dto.UpdateProfileRequest;
import com.indore.entity.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    User saveUser(User user);
    User getById(Long id);
    void deleteUser(Long id);

    void updateProfile(String email, UpdateProfileRequest request);
    void updatePassword(String email, UpdatePasswordRequest request);
    void updateProfileImage(String email, String imageUrl);
}
