package com.plee.library.repository.book;

import com.plee.library.domain.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookRepositoryCustom {

    Page<Book> findBooksWithSearchValue(String searchValue, Pageable pageable);
}