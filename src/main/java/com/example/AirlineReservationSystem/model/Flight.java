package com.example.AirlineReservationSystem.model;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
// import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name="flight")
public class Flight {
    @Id
    private String flightNumber;

    private int price;
    private String origin;
    private String destination;
    private Date departureTime;
    private Date arrivalTime;
    private int seatsLeft;
    private String description;

    @OneToOne(targetEntity=Plane.class, cascade=CascadeType.ALL)
	private Plane plane;

    @OneToMany(cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"flight"})
    private List<Passenger> passengers;

    public Flight(){}
    public Flight(String flightNumber,int price, String origin,String destination,Date departureTime,Date arrivalTime,int seatsLeft,String description,Plane plane,List<Passenger> passengers){
        super();
        this.flightNumber = flightNumber;
        this.price = price;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.seatsLeft = seatsLeft;
        this.description = description;
        this.plane = plane;
        this.passengers = passengers;
    }

    public String getFlightNumber(){
        return flightNumber;
    }
    public void setFlihtNumber(String flightNumber){
        this.flightNumber = flightNumber;
    }
    public int getPrice(){
        return price;
    }
    public void setPrice(int price){
        this.price = price;
    }
    public String getOrigin(){
        return origin;
    }
    public void setOrigin(String origin){
        this.origin = origin;
    }
    public String getDestination(){
        return destination;
    }
    public void setDestination(String destination){
        this.destination = destination;
    }
    public Date getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getSeatsLeft() {
        return seatsLeft;
    }

    public void setSeatsLeft(int seatsLeft) {
        this.seatsLeft = seatsLeft;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Plane getPlane() {
        return plane;
    }

    public void setPlane(Plane plane) {
        this.plane = plane;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<Passenger> passengers) {
        this.passengers = passengers;
    }

    @Override
    public int hashCode(){
        return Objects.hash(flightNumber);
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Flight other = (Flight) obj;
        return Objects.equals(flightNumber, other.flightNumber);
    }
}
