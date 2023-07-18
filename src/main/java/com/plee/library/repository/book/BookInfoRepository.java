package com.plee.library.repository.book;

import com.plee.library.domain.book.BookInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookInfoRepository extends JpaRepository<BookInfo, String> {

}
