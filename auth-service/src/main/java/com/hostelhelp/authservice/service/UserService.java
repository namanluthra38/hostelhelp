package com.hostelhelp.authservice.service;

import java.util.Optional;
import java.util.UUID;

import com.hostelhelp.authservice.model.User;
import com.hostelhelp.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }
    @Transactional
    public void deleteByEmail(String email) {
        userRepository.deleteByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
