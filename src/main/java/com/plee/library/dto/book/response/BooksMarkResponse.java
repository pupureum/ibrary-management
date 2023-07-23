package com.plee.library.dto.book.response;

import com.plee.library.domain.book.BookCategory;
import com.plee.library.domain.book.BookInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
public class BooksMarkResponse {
    private final Long id;
    private final int quantity;
    private final int loanableCnt;
    private final BookInfo bookInfo;
    private final boolean isMarked;
    private final BookCategory bookCategory;

    @Builder
    public BooksMarkResponse(Long id, int quantity, int loanableCnt, BookInfo bookInfo, boolean isMarked, BookCategory bookCategory) {
        this.id = id;
        this.quantity = quantity;
        this.loanableCnt = loanableCnt;
        this.bookInfo = bookInfo;
        this.isMarked = isMarked;
        this.bookCategory = bookCategory;
    }
}
