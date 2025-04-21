package com.example.AirlineReservationSystem.repos;

import com.example.AirlineReservationSystem.model.Plane;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface Planerepo extends JpaRepository<Plane,String>{
    Optional<Plane> findById(String id);
}
