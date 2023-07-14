package com.plee.library.dto.book.response;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookInfo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class AllBooksMarkInfoResponse {
    private final Long id;
    private final int quantity;
    private final int loanableCnt;
    private final BookInfo bookInfo;
    private final boolean isMarked;

    @Builder
    public AllBooksMarkInfoResponse(Long id, int quantity, int loanableCnt, BookInfo bookInfo, boolean isMarked) {
        this.id = id;
        this.quantity = quantity;
        this.loanableCnt = loanableCnt;
        this.bookInfo = bookInfo;
        this.isMarked = isMarked;
    }
}
