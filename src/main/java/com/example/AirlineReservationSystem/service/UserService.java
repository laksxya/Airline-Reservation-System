package com.example.AirlineReservationSystem.service;

import com.example.AirlineReservationSystem.model.User;
import com.example.AirlineReservationSystem.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerNewUser(String username, String password, String role) {
        if (userRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        User newUser = new User(username, passwordEncoder.encode(password), role);
        return userRepository.save(newUser);
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}