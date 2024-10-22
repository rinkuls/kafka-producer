package com.kafka.producer.Kafka.Producer.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class Student implements Serializable {

	private static final long serialVersionUID = 1345L;

	private  Long empId;
	private  String firstName;
	private  String lastName;
	private  Integer age;



}
