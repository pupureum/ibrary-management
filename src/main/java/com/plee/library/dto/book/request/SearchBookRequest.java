package com.plee.library.dto.book.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class SearchBookRequest {

    @NotBlank(message = "검색어를 입력해주세요.")
    private String keyword;
}
