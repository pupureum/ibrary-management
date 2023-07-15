package com.plee.library.dto.book.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class BookmarkRequest {
    private int page;
    private String pageInfo;
    private String category;
}
