package com.plee.library.repository.book;

import com.plee.library.domain.book.Book;
import com.plee.library.dto.book.condition.BookSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookRepositoryCustom {
    Page<Book> search(BookSearchCondition condition, Pageable pageable);
}