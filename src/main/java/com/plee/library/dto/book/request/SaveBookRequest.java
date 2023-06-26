package com.plee.library.dto.book.request;

import com.plee.library.domain.book.BookInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.Range;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SaveBookRequest {

    @NotBlank
    @Size(min = 13, max = 13)
    private String isbn;

    @NotEmpty
    private String title;

    @NotEmpty
    private String author;

    @NotEmpty
    private String publisher;

    @NotEmpty
    private String image;

    @NotEmpty
    private String description;

    @NotEmpty
    private String pubDate;

    @NotBlank
    @Range(min = 1, max = 9999)
    private int quantity;

    public BookInfo toEntity() {
        return BookInfo.builder()
                .isbn(this.isbn)
                .title(this.title)
                .author(this.author)
                .publisher(this.publisher)
                .image(this.image)
                .description(this.description)
                .pubDate(this.pubDate)
                .build();
    }
}
