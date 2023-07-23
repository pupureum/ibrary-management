package com.plee.library.dto.book.request;

import com.plee.library.domain.book.BookInfo;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.ISBN;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReturnBookRequest {

    @NotNull
    private Long historyId;

    @NotNull
    @ISBN
    private String bookInfoIsbn;

    @NotNull
    private boolean onLoan;
}
