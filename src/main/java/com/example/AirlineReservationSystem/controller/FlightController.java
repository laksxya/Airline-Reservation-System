package com.example.AirlineReservationSystem.controller;

import com.example.AirlineReservationSystem.Util.ExceptionHandle;
import com.example.AirlineReservationSystem.Util.Response;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.AirlineReservationSystem.Util.BadRequest;
import com.example.AirlineReservationSystem.model.Flight;
import java.util.List;
import com.example.AirlineReservationSystem.service.FlightService;

import java.text.ParseException;

@RestController
public class FlightController {

	@Autowired
	private FlightService flightService;

	@RequestMapping(value = "/displayflights", method = RequestMethod.GET, produces = {"application/json", "application/xml"})
    public ResponseEntity<?> displayFlights() {
        try {
            List<Flight> flights = flightService.getAllFlights();
            return ResponseEntity.status(HttpStatus.OK).body(flights);
        } catch (NotFoundException ex) {
            return ResponseEntity.badRequest().body(new ExceptionHandle(new BadRequest(404, ex.getMessage())));
        }
    }

	@RequestMapping(value = "/flight/{flightNumber}", method = RequestMethod.GET, produces = { "application/json",
			"application/xml" })
	public ResponseEntity<?> getFlightByNumber(@PathVariable("flightNumber") String flightNumber,
			@RequestParam(value = "xml", required = false) String xml) {
		try {
			return flightService.getFlightByNumber(flightNumber);
		} catch (NotFoundException ex) {
			return ResponseEntity.badRequest().body(new ExceptionHandle(new BadRequest(404, ex.getMessage())));
		}
	}

	@RequestMapping(value = "/airline/{flightNumber}", method = RequestMethod.DELETE, produces = { "application/json",
			"application/xml" })
	public ResponseEntity<?> deleteFlight(@PathVariable("flightNumber") String flightNumber,
			@RequestParam(value = "xml", required = false) String xml) {
		try {
			flightService.deleteFlight(flightNumber);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new Response(200, "Flight " + flightNumber + " has been deleted successfully."));
		} catch (NotFoundException ex) {
			return ResponseEntity.badRequest().body(new ExceptionHandle(new BadRequest(404, ex.getMessage())));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(new ExceptionHandle(new BadRequest(400, ex.getMessage())));
		}
	}

	@RequestMapping(value = "/flight/{flightNumber}", method = RequestMethod.POST, produces = { "application/json",
			"application/xml" })
	public ResponseEntity<?> updateFlight(@PathVariable("flightNumber") String flightNumber,
			@RequestParam("price") int price, @RequestParam("origin") String origin,
			@RequestParam("destination") String destination, @RequestParam("departureTime") String departureTime,
			@RequestParam("arrivalTime") String arrivalTime, @RequestParam("description") String description,
			@RequestParam("capacity") int capacity, @RequestParam("model") String model,
			@RequestParam("manufacturer") String manufacturer, @RequestParam("yearOfManufacture") int yearOfManufacture,
			@RequestParam(value = "xml", required = false) String xml) {
		try {
			System.out.println("Processing flight " + flightNumber + " with departure: " + departureTime + ", arrival: " + arrivalTime);
			
			return flightService.updateFlight(flightNumber, price, origin, destination, departureTime, arrivalTime,
					description, capacity, model, manufacturer, yearOfManufacture);
		} catch (IllegalArgumentException ex) {
			System.out.println("Validation error with flight data: " + ex.getMessage());
			return ResponseEntity.badRequest().body(new ExceptionHandle(new BadRequest(400, ex.getMessage())));
		} catch (ParseException ex) {
			System.out.println("Date parsing error: " + ex.getMessage());
			return ResponseEntity.badRequest().body(new ExceptionHandle(new BadRequest(400, ex.getMessage())));
		} catch (Exception ex) {
			// Catch all other exceptions and provide detailed error messaging
			System.out.println("Unexpected error processing flight: " + ex.getMessage());
			ex.printStackTrace(); // Print full stack trace to logs
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ExceptionHandle(new BadRequest(500, "Server error: " + ex.getMessage())));
		}
	}
}