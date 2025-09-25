package com.hostelhelp.authservice.service;

import java.util.Optional;

import com.hostelhelp.authservice.model.User;
import com.hostelhelp.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
