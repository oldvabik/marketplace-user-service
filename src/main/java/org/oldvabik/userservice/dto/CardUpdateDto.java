package org.oldvabik.userservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class CardUpdateDto {
    @Size(min = 19, max = 19)
    private String number;

    @Future
    private LocalDate expirationDate;
}
