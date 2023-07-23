package com.plee.library.dto.book.request;

import com.plee.library.domain.book.BookInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.ISBN;

@Getter
@Setter
@NoArgsConstructor
public class AddBookRequest {

    @NotBlank
    @ISBN(message = "ISBN 값이 올바르지 않습니다.")
    private String isbn;

    @NotEmpty
    private String title;

    private String author;

    private String publisher;

    private String image;

    private String description;

    private String pubDate;

    @NotBlank(message = "요청 사유를 입력해주세요")
    @Size(min = 10, max = 200, message = "요청 사유를 최소 10자 이상 200자 이내로 입력해주세요")
    private String reqReason;

    @Builder
    public AddBookRequest(String isbn, String title, String author, String publisher, String image, String description, String pubDate, String reqReason) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.image = image;
        this.description = description;
        this.pubDate = pubDate;
        this.reqReason = reqReason;
    }

    public BookInfo toEntity() {
        String modifiedAuthor = this.author.replace("^", ", ");
        return BookInfo.builder()
                .isbn(this.isbn)
                .title(this.title)
                .author(modifiedAuthor)
                .publisher(this.publisher)
                .image(this.image)
                .description(this.description)
                .pubDate(this.pubDate)
                .build();
    }
}
