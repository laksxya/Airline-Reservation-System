package com.example.AirlineReservationSystem.repos;

import com.example.AirlineReservationSystem.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.AirlineReservationSystem.model.Passenger;

import java.util.*;

public interface Passengerrepo extends JpaRepository<Passenger,Integer>{
    Passenger findByPhone(String phone);
    Optional<Passenger> findById(String id);
}
