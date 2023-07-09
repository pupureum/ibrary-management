package com.plee.library.dto.book.request;

import com.plee.library.domain.book.BookInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.ISBN;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddBookRequest {

    @NotBlank
    @ISBN
    private String isbn;

    @NotEmpty
    private String title;

    private String author;

    private String publisher;

    private String image;

    private String description;

    private String pubDate;

    @NotEmpty
    @Size(min = 10, max = 200, message = "요청 사유를 최소 10자 이상 입력해주세요.")
    private String reqReason;

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
