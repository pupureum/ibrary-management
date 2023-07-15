package com.plee.library.repository.book;

import com.plee.library.config.TestJPAConfig;
import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookCategory;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.dto.book.condition.BookSearchCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestJPAConfig.class)
@DisplayName("BookRepositoryCustom 테스트")
class BookRepositoryCustomTest {

    @Autowired
    BookRepository bookRepository;

    @Autowired
    BookCategoryRepository bookCategoryRepository;

    @Autowired
    BookInfoRepository bookInfoRepository;

    private List<Book> books = new ArrayList<>();

    @BeforeEach
    void setUpBook() {
        BookCategory category = BookCategory.builder()
                .categoryName("category")
                .build();
        bookCategoryRepository.save(category);

        // 테스트를 위해 제목 bookInfo1 ~ bookInfo8을 가진 8개의 Book 생성
        for (int i = 1; i <= 8; i++) {
            BookInfo bookInfo = BookInfo.builder()
                    .isbn("978899449208" + i)
                    .title("bookInfo" + i)
                    .author("q")
                    .build();
            bookInfoRepository.save(bookInfo);


            Book book = Book.builder()
                    .bookInfo(bookInfo)
                    .quantity(1)
                    .category(category)
                    .build();
            bookRepository.save(book);
        }

        // 모든 책을 생성순으로 조회하여 books에 저장
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt"));
        books = bookRepository.findAll(pageable).getContent();
    }

    @Nested
    @DisplayName("제목 또는 저자와 일치하는 도서 검색 테스트")
    public class findBooksWithSearchValueTest {

        @Test
        @DisplayName("성공 테스트: 제목과 일치")
        void findBooksWithSearchValue_title() {
            // given
            String searchValue = "Info3";
            Pageable pageable = PageRequest.of(0, 10);

            // when
            BookSearchCondition condition = BookSearchCondition.builder()
                    .keyword(searchValue)
                    .author(true)
                    .title(true)
                    .build();
            Page<Book> result = bookRepository.search(condition, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            // bookInfo3 책 정보는 books.get(2)에 존재
            assertThat(result.getContent().contains(books.get(2))).isTrue();
        }

        @Test
        @DisplayName("성공 테스트: 저자와 일치")
        void findBooksWithSearchValue_author() {
            // given
            String searchValue = "q";
            Pageable pageable = PageRequest.of(0, 10);

            // when
            BookSearchCondition condition = BookSearchCondition.builder()
                    .keyword(searchValue)
                    .author(true)
                    .title(true)
                    .build();
            Page<Book> result = bookRepository.search(condition, pageable);

            // then

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(8);
            assertThat(result.getContent()).usingRecursiveAssertion().isEqualTo(books);
        }

        @Test
        @DisplayName("특정 페이지 검색 결과 조회 테스트")
        void findSecondPageBooksWithSearchValue() {
            // given
            String searchValue = "bookInfo";
            Pageable pageable = PageRequest.of(1, 5);

            // when
            BookSearchCondition condition = BookSearchCondition.builder()
                    .keyword(searchValue)
                    .author(true)
                    .title(true)
                    .build();
            Page<Book> result = bookRepository.search(condition, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent().size()).isEqualTo(3);
            assertThat(result.getContent().contains(books.get(5))).isTrue();
            assertThat(result.getContent().contains(books.get(6))).isTrue();
            assertThat(result.getContent().contains(books.get(7))).isTrue();
        }

        @Test
        @DisplayName("검색 결과가 없는 경우")
        void findBooksWithNotExistSearchResult() {
            // given
            String searchValue = "test";
            Pageable pageable = PageRequest.of(1, 5);

            // when
            BookSearchCondition condition = BookSearchCondition.builder()
                    .keyword(searchValue)
                    .author(true)
                    .title(true)
                    .build();
            Page<Book> result = bookRepository.search(condition, pageable);

            // then
            assertThat(result).isEmpty();
        }
    }
}