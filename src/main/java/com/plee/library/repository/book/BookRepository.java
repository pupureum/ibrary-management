package com.plee.library.repository.book;

import com.plee.library.domain.book.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BookRepository extends JpaRepository<Book, Long>, QuerydslPredicateExecutor<Book>, BookRepositoryCustom {
     Optional<Book> findByBookInfoIsbn(String isbn);

     List<Book> findByBookInfoIsbnIn(Set<String> isbns);

     Page<Book> findByBookCategoryId(Long categoryId, Pageable pageable);

     List<Book> findTop4ByOrderByCreatedAtDesc();

     boolean existsByBookInfoIsbn(String isbn);
}