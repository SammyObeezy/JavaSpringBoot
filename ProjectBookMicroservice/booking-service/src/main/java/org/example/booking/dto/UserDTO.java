package org.example.booking.dto;


import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastname;
}
