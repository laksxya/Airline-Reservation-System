package com.example.AirlineReservationSystem.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index"; 
    }
    
    // Remove these duplicate mappings
    /*
    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    */
    
    @GetMapping("/showAllFlights")
    public String showAllFlights() {
        return "showAllFlights"; 
    }
    
    @GetMapping("/showParticularFlightDetails")
    public String showParticularFlightDetails() {
        return "showParticularFlightDetails"; 
    }
    
    @GetMapping("/addUpdateFlights")
    public String addUpdateFlights() {
        return "addUpdateFlights"; 
    }
    
    @GetMapping("/deleteFlight")
    public String deleteFlight() {
        return "deleteFlight"; 
    }
    
    @GetMapping("/createPassenger")
    public String createPassenger() {
        return "createPassenger"; 
    }
    
    @GetMapping("/getPassengerDetails")
    public String getPassengerDetails() {
        return "getPassengerDetails"; 
    }
    
    @GetMapping("/updatePassenger")
    public String updatePassenger() {
        return "updatePassenger"; 
    }
    
    @GetMapping("/deletePassenger")
    public String deletePassenger() {
        return "deletePassenger"; 
    }
    
    @GetMapping("/createReservation")
    public String createReservation() {
        return "createReservation"; 
    }
    
    @GetMapping("/getReservation")
    public String getReservation() {
        return "getReservation"; 
    }
    
    @GetMapping("/updateReservation")
    public String updateReservation() {
        return "updateReservation"; 
    }
    
    @GetMapping("/cancelReservation")
    public String cancelReservation() {
        return "cancelReservation"; 
    }
}