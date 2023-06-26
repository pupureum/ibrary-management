package com.plee.library.dto.book.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.ISBN;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoanBookRequest {

    @ISBN
    @NotBlank
    private String isbn;
}
