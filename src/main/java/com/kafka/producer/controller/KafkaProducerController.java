package com.kafka.producer.controller;

import com.kafka.producer.model.EmployeeDTO;
import com.kafka.producer.service.KafkaSender;
import com.rinkul.avro.schema.EmployeeRecord;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/raiseException")
public class KafkaProducerController {

    @Autowired
    private KafkaSender sender;

    @PostMapping(value = "/employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid Employee data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<String> sendData(@RequestBody EmployeeDTO employeeDTO) {
        EmployeeRecord employeeRecord = EmployeeRecord.newBuilder().build();
        employeeRecord.setEmpId(employeeDTO.getEmpId());
        employeeRecord.setFirstName(employeeDTO.getFirstName());
        employeeRecord.setLastName(employeeDTO.getLastName());
        employeeRecord.setEmail(employeeDTO.getEmail());
        sender.send(employeeRecord);
        return new ResponseEntity<>("Data sent to Kafka", HttpStatus.OK);
    }

    @PostMapping(value = "/object")
    public ResponseEntity<String> sendAnyData(@RequestBody Object producerObject) {
        sender.send(producerObject);
        return new ResponseEntity<>("Data sent to Kafka", HttpStatus.OK);
    }


    @GetMapping(value = "/health", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "health is fine"),
            @ApiResponse(responseCode = "400", description = "Invalid  data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }
}
