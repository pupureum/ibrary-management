package com.plee.library.dto.book.response;

import com.plee.library.domain.book.BookCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {

    private final Long id;
    private final String categoryName;

    public static CategoryResponse from(BookCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .categoryName(category.getCategoryName())
                .build();
    }
}