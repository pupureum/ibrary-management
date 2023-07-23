package com.plee.library.dto.book.response;

import com.plee.library.domain.book.Book;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class BookInfoResponse {
    private final String title;
    private final String image;

    @Builder
    public BookInfoResponse(String title, String image) {
        this.title = title;
        this.image = image;
    }

    public static List<BookInfoResponse> from(List<Book> books) {
        return books.stream()
                .map(book -> BookInfoResponse.builder()
                        .image(book.getBookInfo().getImage())
                        .title(book.getBookInfo().getTitle())
                        .build())
                .collect(Collectors.toList());
    }
}
