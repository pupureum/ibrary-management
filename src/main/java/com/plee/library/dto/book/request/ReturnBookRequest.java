package com.plee.library.dto.book.request;

import com.plee.library.domain.book.BookInfo;
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

    @NotNull
    private Long historyId;

    @NotNull
    @ISBN
    private String bookInfoIsbn;
}
