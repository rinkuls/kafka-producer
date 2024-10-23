package com.kafka.producer.controller;

import com.kafka.producer.model.Student;
import com.kafka.producer.service.KafkaSender;
import com.rinkul.avro.schema.StudentRecord;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/kafkaProducer")
public class KafkaProducerController {

	@Autowired
	private KafkaSender sender;

	@PostMapping
	public ResponseEntity<String> sendData(@RequestBody Student student){
		StudentRecord studentRec= StudentRecord.newBuilder().build();
		studentRec.setEmpId(student.getEmpId());
		studentRec.setFirstName(student.getFirstName());
		studentRec.setLastName(student.getLastName());
		studentRec.setAge(student.getAge());
		sender.send(studentRec);
		return new ResponseEntity<>("Data sent to Kafka", HttpStatus.OK);
	}
	@GetMapping(value = "/health", produces = "application/json")
	@ApiOperation(value = "health check for application", notes = "")
	@ApiResponses({@ApiResponse(code = 200, message = "Successful response"), @ApiResponse(code = 500, message = "Application Down")})
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}
}
