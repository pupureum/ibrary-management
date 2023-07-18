package com.plee.library.domain.book;

import com.plee.library.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "book_info")
public class BookInfo extends BaseTimeEntity implements Persistable<String> {

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

    @Builder
    public BookInfo(String isbn, String title, String author, String publisher, String image, String description, String pubDate) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.image = image;
        this.description = description;
        this.pubDate = pubDate;
    }

    public void updateAuthor(String author) {
        this.author = author;
    }

    @Override
    public String getId() {
        return isbn;
    }

    @Override
    public boolean isNew() {
        return createdAt == null;
    }
}
