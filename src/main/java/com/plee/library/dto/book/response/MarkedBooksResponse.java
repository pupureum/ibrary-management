package com.plee.library.dto.book.response;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.member.MemberBookmark;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class MarkedBooksResponse {
    private final Long id;
    private final Book book;

    @Builder
    private MarkedBooksResponse(Long id, Book book) {
        this.id = id;
        this.book = book;
    }

    public static List<MarkedBooksResponse> from(Page<MemberBookmark> bookmarkedBooks) {
        return bookmarkedBooks.stream()
                .map(book -> MarkedBooksResponse.builder()
                        .id(book.getId())
                        .book(book.getBook())
                        .build())
                .collect(Collectors.toList());
    }
}
