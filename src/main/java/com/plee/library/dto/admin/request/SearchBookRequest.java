package com.plee.library.dto.admin.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class SearchBookRequest {

    @NotBlank(message = "검색어를 입력해주세요.")
    private String keyword;

    private boolean title = true;

    private boolean author = true;

    private Long categoryId;

    private int before;
}
