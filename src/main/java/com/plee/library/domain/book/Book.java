package com.plee.library.domain.book;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_seq", updatable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String publisher;

    @Column(length = 13, nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private String description;

    @Column(name = "pub_date", nullable = false)
    private Date pubDate;

    @Column(nullable = false)
    private int stock;

    @Column(name = "is_loanable", nullable = false)
    private boolean isLoanable;

    @Builder
    public Book(String title, String author, String publisher, String isbn, String image, String description, Date pubDate, int stock, boolean isLoanable) {
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.isbn = isbn;
        this.image = image;
        this.description = description;
        this.pubDate = pubDate;
        this.stock = stock;
        this.isLoanable = isLoanable;
    }
}
