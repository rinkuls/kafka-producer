package com.kafka.producer.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class EmployeeDTO implements Serializable {

    private static final long serialVersionUID = 1332432422545L;

    private Long empId;
    private String firstName;
    private String lastName;
    private String email;


}
