package com.plee.library.domain.book;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

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

    @Column(name = "author")
    private String author;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "image")
    private String image;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "pub_date")
    private String pubDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private BookCategory category;

    @Builder
    public BookInfo(String isbn, String title, String author, String publisher, String image, String description, String pubDate, BookCategory category) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.image = image;
        this.description = description;
        this.pubDate = pubDate;
        this.category = category;
    }

    public void updateAuthor(String author) {
        this.author = author;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookInfo bookInfo = (BookInfo) o;
        return Objects.equals(isbn, bookInfo.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }
}
