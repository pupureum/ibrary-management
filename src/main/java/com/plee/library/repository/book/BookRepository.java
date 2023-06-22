package com.plee.library.repository.book;

import com.plee.library.domain.book.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
     Optional<Book> findByBookInfoIsbn(String isbn);
}
