package com.example.AirlineReservationSystem.repos;

import com.example.AirlineReservationSystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
}