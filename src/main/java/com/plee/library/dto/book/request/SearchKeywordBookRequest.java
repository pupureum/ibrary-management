package com.plee.library.dto.book.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class SearchKeywordBookRequest {

    @NotBlank(message = "검색어를 입력해주세요.")
    private String keyword;

    private boolean title = true;

    private boolean author = true;

    private int before;
}
