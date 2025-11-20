package org.oldvabik.userservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CardCreateDto {
    @NotBlank
    @Size(min = 19, max = 19)
    private String number;

    @NotNull
    @Future
    private LocalDate expirationDate;

    @NotNull
    private Long userId;
}