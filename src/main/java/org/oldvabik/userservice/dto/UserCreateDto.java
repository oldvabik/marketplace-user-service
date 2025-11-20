package org.oldvabik.userservice.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UserCreateDto {
    @NotBlank
    @Size(min = 3, max = 16)
    private String name;

    @NotBlank
    @Size(min = 3, max = 32)
    private String surname;

    @NotNull
    @Past
    private LocalDate birthDate;

    @NotBlank
    @Email
    private String email;
}
