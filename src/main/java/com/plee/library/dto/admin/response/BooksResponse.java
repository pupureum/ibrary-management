package com.plee.library.dto.admin.response;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookCategory;
import com.plee.library.domain.book.BookInfo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class BooksResponse {
    private final Long id;
    private final int quantity;
    private final int loanableCnt;
    private final BookCategory category;
    private final BookInfo bookInfo;

    @Builder
    public BooksResponse(Long id, int quantity, int loanableCnt, BookCategory category, BookInfo bookInfo) {
        this.id = id;
        this.quantity = quantity;
        this.loanableCnt = loanableCnt;
        this.category = category;
        this.bookInfo = bookInfo;
    }

    public static List<BooksResponse> from(Page<Book> books) {
        return books.stream()
                .map(book -> BooksResponse.builder()
                        .id(book.getId())
                        .quantity(book.getQuantity())
                        .loanableCnt(book.getLoanableCnt())
                        .category(book.getBookCategory())
                        .bookInfo(book.getBookInfo())
                        .build())
                .collect(Collectors.toList());
    }
}
