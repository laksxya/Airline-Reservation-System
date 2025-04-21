package com.example.AirlineReservationSystem.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.GenericGenerator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "Reservation")
public class Reservation {
    @Id
	@GeneratedValue(generator="system-uuid")
	@GenericGenerator(name="system-uuid", strategy = "uuid")
	private String reservationNumber;

    private String origin;
    private String destination;
    private int price;

    @ManyToOne(targetEntity=Passenger.class, cascade=CascadeType.DETACH)
	@JsonIgnoreProperties({"age", "gender","phone","reservations","flight"})
	private Passenger passenger;

    @ManyToMany(targetEntity=Flight.class)
	@JsonIgnoreProperties({"price","seatsLeft","description","plane","passengers"})
	private List<Flight> flights;
	public Reservation(){}
    public Reservation( String origin, String destination, int price, Passenger passenger, List<Flight> flights) {	
		this.origin = origin;
		this.destination = destination;
		this.price = price;
		this.passenger = passenger;
		this.flights=flights;
	}

	public String getReservationNumber() {
		return reservationNumber;
	}

	public void setReservationNumber(String reservationNumber) {
		this.reservationNumber = reservationNumber;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public Passenger getPassenger() {
		return passenger;
	}

	public void setPassenger(Passenger passenger) {
		this.passenger = passenger;
	}

	public List<Flight> getFlights() {
		return flights;
	}

	public void setFlights(List<Flight> flights) {
		this.flights = flights;
	}
}
