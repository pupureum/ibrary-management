package com.plee.library.dto.book.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.ISBN;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReturnBookRequest {

    @NotBlank
    @ISBN
    private String isbn;
}
