package com.plee.library.dto.book.request;

import com.plee.library.domain.book.BookInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.constraints.Range;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveBookRequest {

    @NotBlank
    @ISBN
    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private String image;

    private String description;

    private String pubDate;

    @Range(min = 1, max = 9999, message = "1 ~ 9999 까지의 수량만 입력 가능합니다.")
    private int quantity;

    @NotNull(message = "카테고리를 선택해주세요.")
    private Long categoryId;

    private boolean request;

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
