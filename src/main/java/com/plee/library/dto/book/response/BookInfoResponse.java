package com.plee.library.dto.book.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class BookInfoResponse {

    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private String image;
    private String description;
    private String pubDate;

    @Builder
    public BookInfoResponse(String isbn, String title, String author, String publisher, String image, String description, String pubDate) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.image = image;
        this.description = description;
        this.pubDate = pubDate;
    }
}
