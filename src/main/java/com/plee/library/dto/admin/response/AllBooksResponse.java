package com.plee.library.dto.admin.response;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookInfo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class AllBooksResponse {
    private final Long id;
    private final int quantity;
    private final int loanableCnt;
    private final BookInfo bookInfo;

    @Builder
    public AllBooksResponse(Long id, int quantity, int loanableCnt, BookInfo bookInfo) {
        this.id = id;
        this.quantity = quantity;
        this.loanableCnt = loanableCnt;
        this.bookInfo = bookInfo;
    }

    public static List<AllBooksResponse> from(Page<Book> books) {
        return books.stream()
                .map(book -> AllBooksResponse.builder()
                        .id(book.getId())
                        .quantity(book.getQuantity())
                        .loanableCnt(book.getLoanableCnt())
                        .bookInfo(book.getBookInfo())
                        .build())
                .collect(Collectors.toList());
    }
}
