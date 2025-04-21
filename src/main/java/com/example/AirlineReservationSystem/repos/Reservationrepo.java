package com.example.AirlineReservationSystem.repos;

import com.example.AirlineReservationSystem.model.Flight;
import com.example.AirlineReservationSystem.model.Passenger;
import com.example.AirlineReservationSystem.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface Reservationrepo extends JpaRepository<Reservation,String>{
    Reservation findByReservationNumber(String reservationNumber);
    List<Reservation> findAllByFlightsIn(List<Flight> flights);
    List<Reservation> findByPassenger(Passenger passenger);
}