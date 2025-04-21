package com.example.AirlineReservationSystem.service;

import com.example.AirlineReservationSystem.model.Flight;
import com.example.AirlineReservationSystem.repos.Flightrepo;
import com.example.AirlineReservationSystem.model.Passenger;
import com.example.AirlineReservationSystem.model.Reservation;
import com.example.AirlineReservationSystem.repos.Passengerrepo;
import com.example.AirlineReservationSystem.repos.Reservationrepo;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    @Autowired
    private Reservationrepo reservationRepository;
    
	@Autowired
	private Passengerrepo passengerRepository;
	
	@Autowired
	private Flightrepo flightRepository;

    public ResponseEntity<?> getReservation(String id) throws NotFoundException {
        Optional<Reservation> existingRes=reservationRepository.findById(id);
		if(existingRes.isPresent()){
            Reservation reservation = existingRes.get();
            return new ResponseEntity<>(reservation, HttpStatus.OK);
        }else throw new NotFoundException("Reservation with number " + id + " does not exist");
    }
    
   public ResponseEntity<?> createReservation(String passengerId, List<String> flightNumbers) {
    	Optional<Passenger> passenger = passengerRepository.findById(passengerId);
    	
    	if(passenger.isPresent() && !CollectionUtils.isEmpty(flightNumbers)) {
    		// For clarity and to avoid issues, use only the first flight number provided
    		String flightNumber = flightNumbers.get(0).trim();
    		System.out.println("flight number: " + flightNumber);
    		
    		Optional<Flight> flightOpt = flightRepository.getFlightByFlightNumber(flightNumber);
    		if (!flightOpt.isPresent()) {
    		    throw new IllegalArgumentException("Flight with number " + flightNumber + " does not exist.");
    		}
    		
    		Flight flight = flightOpt.get();
    		
    		// Check if passenger already has a booking for this flight
    		// This is a more accurate check that looks at the actual collection contents
    		for (Passenger existingPassenger : flight.getPassengers()) {
    		    if (existingPassenger.getId().equals(passengerId)) {
    		        throw new IllegalArgumentException("Passenger already has a booking for flight " + flightNumber + 
    		            ". A passenger cannot book the same flight twice.");
    		    }
    		}
    		
    		// Check if seat is available
    		if (flight.getSeatsLeft() <= 0) {
    		    throw new IllegalArgumentException("Cannot create reservation. No seats available on flight " + flightNumber + ".");
    		}
    		
    		try {
    		    // Create the reservation
    		    int fare = flight.getPrice();
    		    
    		    // Create reservation with empty flight list first (to avoid constraint issues)
    		    Reservation newReservation = new Reservation(
    		        flight.getOrigin(), 
    		        flight.getDestination(), 
    		        fare, 
    		        passenger.get(),
    		        new ArrayList<>()  // Start with empty flight list
    		    );
    		    
    		    // First save the reservation without the flight relationship
    		    Reservation savedReservation = reservationRepository.save(newReservation);
    		    
    		    // Update passenger's reservations list
    		    // (This is already done by the constructor and bidirectional relationship)
    		    
    		    // Update seat count
    		    flight.setSeatsLeft(flight.getSeatsLeft() - 1);
    		    
    		    // Now manually update the flight's passengers and save the flight
    		    if (!flight.getPassengers().contains(passenger.get())) {
    		        flight.getPassengers().add(passenger.get());
    		        flightRepository.save(flight);
    		    }
    		    
    		    // Now add the flight to the reservation and update
    		    List<Flight> flightList = new ArrayList<>();
    		    flightList.add(flight);
    		    savedReservation.setFlights(flightList);
    		    
    		    Reservation result = reservationRepository.save(savedReservation);
    		    return new ResponseEntity<>(result, HttpStatus.OK);
    		} catch (Exception e) {
    		    // Rollback seat count on error
    		    flight.setSeatsLeft(flight.getSeatsLeft() + 1);
    		    flightRepository.save(flight);
    		    
    		    if (e.getMessage() != null && (e.getMessage().contains("Duplicate entry") || 
    		        e.getCause() instanceof java.sql.SQLIntegrityConstraintViolationException)) {
    		        throw new IllegalArgumentException("Cannot create reservation. Passenger is already booked on this flight. Error details: " + e.getMessage());
    		    }
    		    throw e;
    		}
    	} else {
    	    throw new IllegalArgumentException("Cannot create reservation. Invalid passenger ID or missing flight number.");
    	}
    }
   
    public ResponseEntity<?> updateReservation( String number, List<String> flightsAdded, List<String>  flightsRemoved) throws NotFoundException {
    	Reservation existingReservation= reservationRepository.findByReservationNumber(number); 
    	if(existingReservation!=null) {
    		List<Flight> existingFlightList = existingReservation.getFlights();
    		if( CollectionUtils.isEmpty(flightsAdded) && !CollectionUtils.isEmpty(flightsRemoved) && existingFlightList.size()<= flightsRemoved.size()) {
    			throw new IllegalArgumentException("Can not update, Reservation has lesser or equal flights user trying to remove");
    		}
    		Passenger passenger= existingReservation.getPassenger();
        	String passengerId= passenger.getId();
        	
        	int existingPrice = existingReservation.getPrice();
        	if(!CollectionUtils.isEmpty(flightsRemoved)) {
        		System.out.println("flightsRemoved: " +flightsRemoved);
        		List<String> trimmedFlightsRemoved = flightsRemoved.stream()
        		        .map(String::trim)
        		        .collect(Collectors.toList());
    			List<Flight> flightListToRemove = getFlightList(trimmedFlightsRemoved);
    			if(existingFlightList.size()!=0) {
    				existingFlightList.removeAll(flightListToRemove);
    				increaseAvailableFlightSeats(flightListToRemove);	
    			}
    			int newPrice=existingPrice-calculatePrice(flightListToRemove);
    			if(existingFlightList.size()>0) {
    				existingReservation.setOrigin(existingFlightList.get(0).getOrigin());
        			existingReservation.setDestination(existingFlightList.get(existingFlightList.size()-1).getDestination());
    			}
    			existingReservation.setPrice(newPrice);	
    		}
    		if(!CollectionUtils.isEmpty(flightsAdded)) {
    			System.out.println("flightsAdded: " +flightsAdded);
    			List<String> trimmedFlightsAdded = flightsAdded.stream()
        		        .map(String::trim)
        		        .collect(Collectors.toList());
    			List<Flight> flightListToAdd = getFlightList(trimmedFlightsAdded);
    			if(flightListToAdd.size()>1) {
        			boolean isFirstTimeOverlap=isTimeOverlapWithinReservation(flightListToAdd);
            		if(isFirstTimeOverlap) {
            	    	System.out.println("Time overlap within same reservation");
            			throw new IllegalArgumentException("Can not update, there is time overlap between the flights within same reservation");
            		}
            		boolean isSecondTimeOverlap=isTimeOverlapForSamePerson( passengerId,flightListToAdd);
            		if(isSecondTimeOverlap) {
            			System.out.println("Time overlap within for same Person");
            			throw new IllegalArgumentException("Can not update, the same person cannot have two reservations that overlap with each other");
            		}
        		}
    			if(isSeatsAvailable(flightListToAdd)) {
	    			int newPrice=existingPrice+calculatePrice(flightListToAdd);
	    			existingFlightList.addAll(flightListToAdd);
	    			existingReservation.setFlights(existingFlightList);
	    			existingReservation.setOrigin(existingFlightList.get(0).getOrigin());
	    			existingReservation.setDestination(existingFlightList.get(existingFlightList.size()-1).getDestination());
	    			existingReservation.setPrice(newPrice);
	    			reduceAvailableFlightSeats(flightListToAdd);
    			}else {
    				throw new IllegalArgumentException("No seats available. Flight capacity full.");
    			}
    			
    		}

    		Reservation resUpdate =reservationRepository.save(existingReservation);
    		return new ResponseEntity<>(resUpdate, HttpStatus.OK);
    		
    	}else {
    		 throw new NotFoundException( "No reservation found for given reservation number");
    	}
    	
    }
    
    public ResponseEntity<?> cancelReservation( String reservationNumber) throws NotFoundException {
    	Reservation res=reservationRepository.findByReservationNumber(reservationNumber);
        if(res !=null){
        	
        	res.getPassenger().getReservations().remove(res);
        	reservationRepository.delete(res);
        	List<Flight> flightList=res.getFlights();
        	if(flightList.size()!=0) 
        		increaseAvailableFlightSeats(flightList);
        	return new ResponseEntity<>("Reservation with number " + reservationNumber + " is canceled successfully ", HttpStatus.OK);
        }else {
            throw new NotFoundException("Reservation with number "+reservationNumber+" does not exist");
        } 	
    }
    
    private  boolean isTimeOverlapWithinReservation( List<Flight> flightList ) {
    	for(int i=0;i <flightList.size(); i++) {
    		for(int j=i+1; j<flightList.size();j++ ) {
    			Date startDate1 = flightList.get(i).getDepartureTime();
    			Date endDate1 =  flightList.get(i).getArrivalTime();
    			Date startDate2 = flightList.get(j).getDepartureTime();
    			Date endDate2 = flightList.get(j).getArrivalTime();
    			 if (startDate1.compareTo(endDate2) <= 0 && endDate1.compareTo(startDate2) >= 0) {
    				 return true; 
    			 }
    		}
    		
    	}
    	return false;
    	
    }
    
    private boolean isTimeOverlapForSamePerson(String passengerId, List<Flight> flightList) {
    	Optional<Passenger> passenger = passengerRepository.findById(passengerId);
    	List<Reservation> reservationList = passenger.get().getReservations();
    	List<Flight> currentFlightList=new ArrayList<Flight>();
    
		for(Reservation res:reservationList){
			for(Flight flight:res.getFlights()){
				currentFlightList.add(flight);
			}
		}
		System.out.println("existing Reservations for current passeneger: " +currentFlightList );
		for(int i=0; i < flightList.size(); i++) {
			for(int j=0;j<currentFlightList.size();j++) {
				Date startDate1 = flightList.get(i).getDepartureTime();
    			Date endDate1 =  flightList.get(i).getArrivalTime();
    			Date startDate2 = currentFlightList.get(j).getDepartureTime();
    			Date endDate2 = currentFlightList.get(j).getArrivalTime();
    			 if (startDate1.compareTo(endDate2) <= 0 && endDate1.compareTo(startDate2) >= 0) {
    				 return true; 
    			 }
			}
		}
    	
    	return false;
    }
   
    public boolean isSeatsAvailable(List<Flight> flightList){
		for(Flight flight : flightList){
			if(flight.getSeatsLeft() <= 0) return false;
		}
		return true;
	}
    
    private List<Flight> getFlightList(List<String> flightNumbers){
    	List<Flight> flightList = new ArrayList<>();
    	for(String flightNumber: flightNumbers ) {
    		Optional<Flight> flight=flightRepository.getFlightByFlightNumber(flightNumber);
    		if(flight.isPresent()) {
    			flightList.add(flight.get());
    		}
    	}
    	return flightList;
    	
    }
    
    public int calculatePrice(List<Flight> flightList) {
 	   int price=0;
 	   for(Flight f:flightList ) {
 		   price+=f.getPrice();
 	   }
 	   return price;
    }
    
    public void reduceAvailableFlightSeats(List<Flight> flightList){
 		for(Flight flight : flightList){
 			flight.setSeatsLeft(flight.getSeatsLeft()-1);
 		}
 		
 	}
    
    public void increaseAvailableFlightSeats(List<Flight> flightList){
 		for(Flight flight : flightList){
 			flight.setSeatsLeft(flight.getSeatsLeft()+1);
 		}
 	}
}