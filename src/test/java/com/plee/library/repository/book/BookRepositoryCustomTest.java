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
import java.util.Collections;
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

    private List<BookCategory> categories;

    @BeforeEach
    void setUpBook() {
        //를 위해 제목 bookInfo1 ~ bookInfo8을 가진 8개의 Book, Category 생성
        for (int i = 1; i <= 8; i++) {
            BookInfo bookInfo = BookInfo.builder()
                    .isbn("978899449208" + i)
                    .title("bookInfo" + i)
                    .author("book author")
                    .build();
            bookInfoRepository.save(bookInfo);

            BookCategory category = BookCategory.builder()
                    .categoryName("category" + i)
                    .build();
            bookCategoryRepository.save(category);

            Book book = Book.builder()
                    .bookInfo(bookInfo)
                    .quantity(1)
                    .category(category)
                    .build();
            bookRepository.save(book);
        }

        // 모든 책을 생성순으로 조회하여 books 에 저장
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt"));
        books = bookRepository.findAll(pageable).getContent();
        categories = bookCategoryRepository.findAll(pageable).getContent();
    }

    @Nested
    @DisplayName("카테고리 정보 없는 경우 도서 검색")
    public class SearchBooksWithoutCategoryTest {

        @Test
        @DisplayName("제목과 일치하는 경우 조회")
        void searchBook_Title() {
            // given
            String searchValue = "Info3";
            Pageable pageable = PageRequest.of(0, 10);

            // when
            BookSearchCondition condition = BookSearchCondition.builder()
                    .keyword(searchValue)
                    .author(false)
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
        @DisplayName("저자에만 일치하는 값이 있는 경우 조회")
        void searchBook_Author() {
            // given
            String searchValue = "author";
            Pageable pageable = PageRequest.of(0, 10);

            // when
            BookSearchCondition condition = BookSearchCondition.builder()
                    .keyword(searchValue)
                    .author(true)
                    .title(false)
                    .build();
            Page<Book> result = bookRepository.search(condition, pageable);

            // then

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(8);
            assertThat(result.getContent()).usingRecursiveAssertion().isEqualTo(books);
        }

        @Test
        @DisplayName("특정 페이지 검색 결과 조회")
        void searchBook_SecondPage() {
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
        void searchBook_NotExist() {
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
    @Nested
    @DisplayName("카테고리 정보 있는 경우 도서 검색")
    public class findBooksWithSearchValueTest {

        @Test
        @DisplayName("카테고리와 제목이 일치하는 경우 조회")
        void searchBook_Title() {
            // given
            String searchValue = "Info3";
            Pageable pageable = PageRequest.of(0, 10);

            // 해당 도서의 카테고리
            BookCategory category = categories.get(2);

            // when
            BookSearchCondition condition = BookSearchCondition.builder()
                    .keyword(searchValue)
                    .categoryId(category.getId())
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
        @DisplayName("카테고리와 저자가 일치하는 경우 조회")
        void searchBook_author() {
            // given
            String searchValue = "author";
            Pageable pageable = PageRequest.of(0, 10);

            // 특정 카테고리
            BookCategory category = categories.get(0);

            // when
            BookSearchCondition condition = BookSearchCondition.builder()
                    .keyword(searchValue)
                    .categoryId(category.getId())
                    .author(true)
                    .title(true)
                    .build();
            Page<Book> result = bookRepository.search(condition, pageable);

            // then
            assertThat(result).isNotNull();
            // 카테고리는 도서마다 독립적이므로 같은 저자의 도서더라도 1건 조회
            assertThat(result.getContent()).hasSize(1).contains(books.get(0));
        }

        @Test
        @DisplayName("검색 결과가 없는 경우")
        void searchBook_notExist() {
            // given
            String searchValue = "test";
            Pageable pageable = PageRequest.of(1, 5);

            // when
            BookSearchCondition condition = BookSearchCondition.builder()
                    .keyword(searchValue)
                    .categoryId(100L)
                    .author(true)
                    .title(true)
                    .build();
            Page<Book> result = bookRepository.search(condition, pageable);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("카테고리와 제목, 저자 통합 검색")
    class SearchByKeyword {
        @Test
        @DisplayName("정렬 기준이 없는 경우")
        void searchBook_WithoutSort() {
            // given
            String searchValue = "book";
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
        @DisplayName("정렬 기준이 있는 경우")
        void searchBook_WithSort() {
            // given
            String searchValue = "book";
            // 생성일자 내림차순 정렬
            Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());

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

            List<Book> reversedBooks = new ArrayList<>(books);
            Collections.reverse(reversedBooks);
            assertThat(result.getContent()).containsExactlyElementsOf(reversedBooks);
        }
    }
}