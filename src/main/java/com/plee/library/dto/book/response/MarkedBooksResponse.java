package com.plee.library.dto.book.response;

import com.plee.library.domain.book.Book;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MarkedBooksResponse {
    private Long id;
    private Book book;

    @Builder
    public MarkedBooksResponse(Long id, Book book) {
        this.id = id;
        this.book = book;
    }
}
