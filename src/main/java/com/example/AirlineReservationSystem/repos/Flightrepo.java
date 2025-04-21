package com.example.AirlineReservationSystem.repos;

import com.example.AirlineReservationSystem.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.AirlineReservationSystem.model.Flight;

import java.util.List;
import java.util.Optional;

public interface Flightrepo extends JpaRepository<Flight,Integer>{
    Optional<Flight> getFlightByFlightNumber(String flightNumber);
}
