package com.plee.library.repository.book;

import com.plee.library.domain.book.BookInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface BookInfoRepository extends JpaRepository<BookInfo, String> {
    @Transactional
    @Modifying
    @Query("DELETE FROM BookInfo b WHERE b.isbn = :isbn")
    void deleteById(String isbn);
}
