package com.example.AirlineReservationSystem.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import com.example.AirlineReservationSystem.model.Passenger;
import com.example.AirlineReservationSystem.model.Plane;
import com.example.AirlineReservationSystem.model.Reservation;
import com.example.AirlineReservationSystem.repos.Passengerrepo;
import com.example.AirlineReservationSystem.repos.Reservationrepo;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.example.AirlineReservationSystem.model.Flight;
import com.example.AirlineReservationSystem.repos.Flightrepo;

@Service
public class FlightService {
    @Autowired
    private Flightrepo flightrepo;
    @Autowired
    private Reservationrepo reservationrepo;
    @Autowired
    private Passengerrepo passengerrepo;

    private boolean checkValidUpdate(Flight currentFlight, Date currentFlightArrivalTime,
									 Date currentFlightDepartureTime) {
		for (Passenger passenger : passengerrepo.findAll()) {
			Set<Flight> flights = new HashSet<>();
			for (Reservation reservation : passenger.getReservations()) {
				flights.addAll(reservation.getFlights());
			}
			if(flights.contains(currentFlight)){
				flights.remove(currentFlight);
				for(Flight flight: flights){
					Date flightDepartureTime = flight.getDepartureTime();
					Date flightArrivalTime = flight.getArrivalTime();
					if (currentFlightArrivalTime.compareTo(flightDepartureTime) >= 0
							&& currentFlightDepartureTime.compareTo(flightArrivalTime) <= 0) {
						return false;
					} 
				}
			}
		}
		return true;
    }

    public ResponseEntity<?> getFlightByNumber(String flightNumber) throws NotFoundException {
        Optional<Flight> res = flightrepo.getFlightByFlightNumber(flightNumber);
        if (res.isPresent()) {
            Flight flight = res.get();
            return new ResponseEntity<>(flight, HttpStatus.OK);
        } else {
            throw new NotFoundException("Sorry, the requested flight with number" + flightNumber + "does not exist");
        }
    }

    public ResponseEntity<?> updateFlight(String flightNumber, int price, String origin, String destination,
            String departureTime, String arrivalTime, String description, int capacity, String model,
            String manufacturer, int yearOfManufacture) throws ParseException {
        try {
            System.out.println("Starting flight update process for: " + flightNumber);
            
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date dTime = formatter.parse(departureTime);
            Date aTime = formatter.parse(arrivalTime);
            
            System.out.println("Parsed departure time: " + dTime);
            System.out.println("Parsed arrival time: " + aTime);
            
            if (origin.equals(destination) || aTime.compareTo(dTime) <= 0) {
                throw new IllegalArgumentException("Illegal arguments entered to create/update flight: Origin and destination cannot be the same, and arrival time must be after departure time.");
            }
            
            Optional<Flight> res = flightrepo.getFlightByFlightNumber(flightNumber);
            Flight flight;
            Plane plane;
            
            if (res.isPresent()) {
                System.out.println("Updating existing flight: " + flightNumber);
                flight = res.get();
                Flight finalFlight = flight;
                List<Reservation> reservationList = reservationrepo.findAllByFlightsIn(
                        new ArrayList<Flight>() {
                            {
                                add(finalFlight);
                            }
                        });
                        
                System.out.println("Found " + reservationList.size() + " existing reservations for this flight");
                
                if (reservationList.size() > capacity) {
                    throw new IllegalArgumentException("Target capacity (" + capacity + ") is less than active reservations (" + reservationList.size() + ")");
                }
                
                if (!checkValidUpdate(flight, aTime, dTime)) {
                    throw new IllegalArgumentException("Flight timings overlap with other passenger reservations");
                }
                
                flight.setPrice(price);
                flight.setOrigin(origin);
                flight.setDestination(destination);
                flight.setDepartureTime(dTime);
                flight.setArrivalTime(aTime);
                flight.setDescription(description);
                flight.setSeatsLeft(capacity);
                flight.getPlane().setCapacity(capacity);
                flight.getPlane().setModel(model);
                flight.getPlane().setManufacturer(manufacturer);
                flight.getPlane().setYearOfManufacture(yearOfManufacture);
            } else {
                System.out.println("Creating new flight: " + flightNumber);
                plane = new Plane(capacity, model, manufacturer, yearOfManufacture);
                flight = new Flight(flightNumber, price, origin, destination, dTime, aTime, capacity, description, plane,
                        new ArrayList<>());
            }
            
            System.out.println("Saving flight to database");
            flight = flightrepo.save(flight);
            System.out.println("Flight saved successfully: " + flightNumber);
            
            return new ResponseEntity<>(flight, HttpStatus.OK);
        } catch (ParseException pe) {
            System.out.println("Date parsing error: " + pe.getMessage());
            throw pe;
        } catch (IllegalArgumentException iae) {
            System.out.println("Validation error: " + iae.getMessage());
            throw iae;
        } catch (Exception e) {
            System.out.println("Unexpected error in flight update: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Server error processing flight: " + e.getMessage(), e);
        }
    }

    public void deleteFlight(String flightNumber) throws NotFoundException {
		Optional<Flight> res = flightrepo.getFlightByFlightNumber(flightNumber);
		if (res.isPresent()) {
			Flight flight = res.get();
			
			// Step 1: Identify all reservations that include this flight
			List<Reservation> reservationList = reservationrepo.findAllByFlightsIn(
					new ArrayList<Flight>() {{add(flight);}});
			
			System.out.println("Preparing to delete flight: " + flightNumber);
			System.out.println("Found " + reservationList.size() + " reservations associated with this flight");
			
			// Step 2: Process each reservation
			for (Reservation reservation : reservationList) {
				System.out.println("Processing reservation: " + reservation.getReservationNumber());
				
				// Step 2a: Get the passenger for this reservation
				Passenger passenger = reservation.getPassenger();
				if (passenger != null) {
					System.out.println("- Found passenger: " + passenger.getId() + " for reservation: " + reservation.getReservationNumber());
					
					// Step 2b: Remove the reservation from the passenger's reservations collection
					if (passenger.getReservations() != null) {
						System.out.println("- Removing reservation from passenger's collection");
						passenger.getReservations().remove(reservation);
						passengerrepo.save(passenger);
					}
				}
				
				// Step 2c: Clear the flights from the reservation
				System.out.println("- Clearing flights from reservation");
				reservation.getFlights().clear();
				reservationrepo.save(reservation);
				
				// Step 2d: Now delete the reservation
				if (reservation.getFlights().size() <= 1) {
					System.out.println("- Deleting reservation: " + reservation.getReservationNumber());
					// Use a direct query to delete from the passenger_reservations table if needed
					// This should be handled by the removal from passenger.getReservations() above
					reservationrepo.delete(reservation);
				} else {
					// Just remove this flight from the reservation
					System.out.println("- Removing flight from reservation and updating price");
					int newPrice = reservation.getPrice() - flight.getPrice();
					reservation.setPrice(Math.max(0, newPrice));
					reservation.getFlights().remove(flight);
					reservationrepo.save(reservation);
				}
			}
			
			// Step 3: Clear flight_passengers relationships (if any exist)
			if (flight.getPassengers() != null && !flight.getPassengers().isEmpty()) {
				System.out.println("Clearing " + flight.getPassengers().size() + " direct passenger relationships");
				flight.getPassengers().clear();
				flightrepo.save(flight);
			}
			
			// Step 4: Store plane ID for later cleanup
			Integer planeId = null;
			if (flight.getPlane() != null) {
				planeId = flight.getPlane().getId();
				System.out.println("Flight has associated plane with ID: " + planeId);
			}
			
			// Step 5: Delete the flight
			System.out.println("Deleting flight: " + flightNumber);
			flightrepo.delete(flight);
			
			// Step 6: Clean up orphaned plane if needed
			if (planeId != null) {
				System.out.println("Plane cleanup would be performed here for ID: " + planeId);
				// planeService.deletePlane(planeId);
			}
			
			System.out.println("Flight deletion process completed successfully");
		} else {
			throw new NotFoundException("Sorry, the requested flight with number " + flightNumber + " does not exist");
		}
	}
    public List<Flight> getAllFlights() throws NotFoundException {
        List<Flight> flights = flightrepo.findAll();
        if (flights.isEmpty()) {
            throw new NotFoundException("No flights found");
        }
        return flights;
    }
}
