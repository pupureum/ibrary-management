package com.plee.library.domain.book;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "book_info")
public class BookInfo {

    @Id
    @Column(name = "book_info_isbn", length = 13, nullable = false, updatable = false)
    private String isbn;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "author", nullable = false)
    private String author;

    @Column(name = "publisher", nullable = false)
    private String publisher;

    @Column(name = "image", nullable = false)
    private String image;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "pub_date", nullable = false)
    private Date pubDate;

    @Builder
    public BookInfo(String isbn, String title, String author, String publisher, String image, String description, Date pubDate) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.image = image;
        this.description = description;
        this.pubDate = pubDate;
    }
}
