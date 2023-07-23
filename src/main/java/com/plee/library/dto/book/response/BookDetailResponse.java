package com.plee.library.dto.book.response;

import com.plee.library.domain.book.Book;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BookDetailResponse {
    private final Book book;

    private final boolean isLoaned;

    private final boolean isMarked;

    public BookDetailResponse(Book book, boolean isLoaned, boolean isMarked) {
        this.book = book;
        this.isLoaned = isLoaned;
        this.isMarked = isMarked;
    }

    public static BookDetailResponse of(Book book, boolean isLoaned, boolean isMarked) {
        return new BookDetailResponse(book, isLoaned, isMarked);
    }
}
