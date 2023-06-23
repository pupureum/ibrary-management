package com.plee.library.dto.book.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AddBookRequest {

    @NotBlank
    @Range(min = 1, max = 9999)
    private int stock_amt;
}
