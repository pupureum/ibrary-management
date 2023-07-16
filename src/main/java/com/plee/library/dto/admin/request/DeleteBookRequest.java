package com.plee.library.dto.admin.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class DeleteBookRequest {

    private Long categoryId;

    private String keyword;

    private int page;
}
