package com.plee.library.dto.book.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchBookResponse {

    private int total;
    private int start;
    private int display;
    private List<SearchBookItem> items;

    @Data
    public static class SearchBookItem {
        private String isbn;
        private String title;
        private String image;
        private String author;
        private String publisher;
        private String description;
        private String pubdate;
        private String link;
    }
}
