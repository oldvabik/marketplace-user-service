package org.oldvabik.userservice.dto;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateDto {
    @Size(min = 3, max = 16)
    private String name;

    @Size(min = 3, max = 32)
    private String surname;

    @Past
    private LocalDate birthDate;
}
