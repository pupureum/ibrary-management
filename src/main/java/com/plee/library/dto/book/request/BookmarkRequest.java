package com.plee.library.dto.book.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Builder
@NoArgsConstructor
public class BookmarkRequest {
    private int page;
    private String pageInfo;
    private String category;

    public BookmarkRequest(int page, String pageInfo, String category) {
        this.page = page;
        this.pageInfo = pageInfo;
        this.category = category;
    }
}
