package com.example.AirlineReservationSystem.controller;

import com.example.AirlineReservationSystem.Util.BadRequest;
import com.example.AirlineReservationSystem.Util.ExceptionHandle;
import com.example.AirlineReservationSystem.Util.Response;
import com.example.AirlineReservationSystem.service.PassengerService;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.sql.SQLException;

@Transactional
@RestController
public class PassengerController {

    @Autowired
    private PassengerService service;

    @RequestMapping(value="/passenger/{id}", method=RequestMethod.PUT, produces={"application/json", "application/xml"})
    public ResponseEntity<?> updatePassenger(
            @PathVariable String id,
            @RequestParam("firstname") String firstname,
            @RequestParam("lastname")  String lastname,
            @RequestParam("age")       String age,
            @RequestParam("gender")    String gender,
            @RequestParam("phone")     String phone,
            @RequestParam(value="xml", required = false) String xml
    ) {
		try{
			return service.updatePassenger(id, firstname,
					lastname, age, gender, phone);
		}catch (IllegalArgumentException ex){
			return ResponseEntity.badRequest().body(new ExceptionHandle(new BadRequest(400, ex.getMessage())));
		}catch (NotFoundException ex){
			return ResponseEntity.badRequest().body(new ExceptionHandle(new BadRequest(404, ex.getMessage())));
		}
    }

    @RequestMapping(value="/passenger/{id}", method=RequestMethod.DELETE, produces={"application/json", "application/xml"})
    public ResponseEntity<?> deletePassenger(
            @PathVariable String id,
            @RequestParam(value="xml", required = false)       String xml
    ) {
		try{
			service.deletePassenger(id);
			return ResponseEntity.status(HttpStatus.OK).body(new Response(200,"Passenger with id" + id + " is deleted successfully "));
		}catch (NotFoundException ex) {
			return ResponseEntity.badRequest().body(new ExceptionHandle(new BadRequest(404, ex.getMessage())));
		}

    }
    
    @RequestMapping(value="/passenger", method=RequestMethod.POST, produces={"application/json", "application/xml"})
	public ResponseEntity<?> createPassenger(
			@RequestParam("firstname") String firstname,
			@RequestParam("lastname") String lastname,
			@RequestParam("age") String age,
			@RequestParam("gender") String gender,
			@RequestParam("phone") String phone,
			@RequestParam(value = "xml", required=false) String xml
			) {
		try {
			return service.createPassenger(firstname, lastname, age, gender, phone);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.badRequest().body(new ExceptionHandle(new BadRequest(400, ex.getMessage())));
		}
	}

    @RequestMapping(value="/passenger/{id}", method=RequestMethod.GET, produces={"application/json", "application/xml"})
	public ResponseEntity<?> getPassenger(
			@PathVariable("id") String id,
			@RequestParam(value = "xml", required=false) String xml
			)
			 {
			 try{
				 return service.getPassenger(id);
			 }catch(NotFoundException e){
				return ResponseEntity.badRequest().body(new ExceptionHandle(new BadRequest(404,e.getMessage())));
			 }
	}

}