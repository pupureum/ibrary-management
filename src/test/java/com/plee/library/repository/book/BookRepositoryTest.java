package com.plee.library.repository.book;

import com.plee.library.config.TestJPAConfig;
import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookInfo;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestJPAConfig.class)
@DisplayName("BookInfoRepository 테스트")
class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookInfoRepository bookInfoRepository;

    private BookInfo bookInfo1;
    private BookInfo bookInfo2;

    @BeforeEach
    void setUpBookInfo() {
        bookInfo1 = BookInfo.builder()
                .isbn("9788994492032")
                .title("Java의 정석")
                .author("남궁성")
                .publisher("도우출판")
                .image("https://shopping-phinf.pstatic.net/main_3246668/32466681076.20230622071100.jpg")
                .description("책 소개입니다")
                .pubDate("20160201")
                .build();
        bookInfoRepository.save(bookInfo1);

        bookInfo2 = BookInfo.builder()
                .isbn("9788966261208")
                .title("HTTP 완벽 가이드")
                .author("안슈 아가왈")
                .publisher("인사이트")
                .image("https://shopping-phinf.pstatic.net/main_3246114/32461143685.20230606105115.jpg")
                .description("책 소개입니다")
                .pubDate("20141215")
                .build();
        bookInfoRepository.save(bookInfo2);
    }

    @Nested
    @DisplayName("도서 생성 테스트")
    public class SaveBookTest {
        @Test
        @DisplayName("성공 테스트")
        void save() {
            // given
            Book book = Book.builder()
                    .bookInfo(bookInfo1)
                    .quantity(5)
                    .build();

            // when
            Book savedBook = bookRepository.save(book);

            // then
            assertThat(savedBook).isNotNull();
            assertThat(savedBook.getId()).isEqualTo(book.getId());
            assertThat(savedBook.getBookInfo()).isEqualTo(book.getBookInfo());
            assertThat(savedBook.getQuantity()).isEqualTo(book.getQuantity());
            assertThat(savedBook.getLoanableCnt()).isEqualTo(book.getLoanableCnt());
        }

        @Test
        @DisplayName("실패 테스트: 이미 도서 정보 존재")
        void saveFailWithDuplicatedBookInfo() {
            // given
            Book book1 = Book.builder()
                    .bookInfo(bookInfo1)
                    .quantity(5)
                    .build();
            bookRepository.save(book1);

            Book book2 = Book.builder()
                    .bookInfo(bookInfo1)
                    .quantity(5)
                    .build();

            // when, then
            assertThatThrownBy(() -> bookRepository.save(book2))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("ID로 도서 조회 테스트")
    public class FindByIdTest {
        @Test
        @DisplayName("성공 테스트")
        void findById() {
            //given
            Book book1 = Book.builder()
                    .bookInfo(bookInfo1)
                    .quantity(3)
                    .build();
            bookRepository.save(book1);
            Book book2 = Book.builder()
                    .bookInfo(bookInfo2)
                    .quantity(1)
                    .build();
            bookRepository.save(book2);

            // when
            Book findBook1 = bookRepository.findById(book1.getId()).orElse(null);

            // then
            assertThat(findBook1).isNotNull();
            assertThat(findBook1.getId()).isEqualTo(book1.getId());
            assertThat(findBook1.getBookInfo()).isEqualTo(book1.getBookInfo());
            assertThat(findBook1.getQuantity()).isEqualTo(book1.getQuantity());
            assertThat(findBook1.getLoanableCnt()).isEqualTo(book1.getLoanableCnt());
        }

        @Test
        @DisplayName("없는 ID로 조회하는 경우")
        void findWithNotExistId() {
            //given
            Long notExistId = 100L;

            // when
            Book findBook1 = bookRepository.findById(100L).orElse(null);

            // then
            assertThat(findBook1).isNull();
        }
    }

    @Nested
    @DisplayName("ISBN 값으로 도서 조회 테스트")
    public class FindByIsbnTest {
        @Test
        @DisplayName("성공 테스트")
        void findByIsbn() {
            //given
            Book book1 = Book.builder()
                    .bookInfo(bookInfo1)
                    .quantity(3)
                    .build();
            bookRepository.save(book1);
            Book book2 = Book.builder()
                    .bookInfo(bookInfo2)
                    .quantity(1)
                    .build();
            bookRepository.save(book2);

            // when
            Book findBook1 = bookRepository.findByBookInfoIsbn(book1.getBookInfo().getIsbn()).orElse(null);

            // then
            assertThat(findBook1).isNotNull();
            assertThat(findBook1.getId()).isEqualTo(book1.getId());
            assertThat(findBook1.getBookInfo()).isEqualTo(book1.getBookInfo());
            assertThat(findBook1.getQuantity()).isEqualTo(book1.getQuantity());
            assertThat(findBook1.getLoanableCnt()).isEqualTo(book1.getLoanableCnt());
        }

        @Test
        @DisplayName("없는 ISBN 값으로 조회하는 경우")
        void findWithNotExistIsbn() {
            //given
            String notExistIsbn = "1234567890123";

            // when
            Book findBook1 = bookRepository.findByBookInfoIsbn(notExistIsbn).orElse(null);

            // then
            assertThat(findBook1).isNull();
        }
    }

    @Nested
    @DisplayName("신규 도서 4권 최근 입고순으로 조회 테스트")
    public class FindTop4ByOrderByCreatedAtDescTest {
        @Test
        @DisplayName("도서가 4권보다 적은 경우")
        void findTop4ByOrderByCreatedAtDesc_WhenLessThan4Books() {
            //given
            Book book1 = Book.builder()
                    .bookInfo(bookInfo1)
                    .quantity(3)
                    .build();
            bookRepository.save(book1);
            Book book2 = Book.builder()
                    .bookInfo(bookInfo2)
                    .quantity(1)
                    .build();
            bookRepository.save(book2);

            // when
            List<Book> books = bookRepository.findTop4ByOrderByCreatedAtDesc();

            // then
            assertThat(books.size()).isEqualTo(2);
            assertThat(books).containsExactly(book2, book1);
        }

        @Test
        @DisplayName("도서가 4권보다 많은 경우")
        void findTop4ByOrderByCreatedAtDesc() throws Exception{
            //given
            Book book1 = Book.builder()
                    .bookInfo(bookInfo1)
                    .quantity(3)
                    .build();
            bookRepository.save(book1);
            Book book2 = Book.builder()
                    .bookInfo(bookInfo2)
                    .quantity(1)
                    .build();
            bookRepository.save(book2);

            List<Book> books = new ArrayList<>();
            for (int i = 3; i <= 5; i++) {
                BookInfo bookInfo = BookInfo.builder()
                        .isbn("978899449208" + i)
                        .title("bookInfo" + i)
                        .author("info" + i)
                        .publisher("info" + i)
                        .image("info" + i + ".jpg")
                        .description("책 소개입니다")
                        .pubDate("20221211")
                        .build();
                bookInfoRepository.save(bookInfo);

                Book book = Book.builder()
                        .bookInfo(bookInfo)
                        .quantity(1)
                        .build();
                bookRepository.save(book);
                books.add(book);
            }

            // when
            List<Book> latestBooks = bookRepository.findTop4ByOrderByCreatedAtDesc();
            // then
            assertThat(latestBooks.size()).isEqualTo(4);
            assertThat(latestBooks).containsExactly(books.get(2), books.get(1), books.get(0), book2);
            assertThat(latestBooks).doesNotContain(book1);
        }
    }

    @Nested
    @DisplayName("도서 전체 조회 테스트")
    public class FindAllTest {
        @Test
        @DisplayName("성공 테스트")
        void findAllBooks() {
            //given
            Book book1 = Book.builder()
                    .bookInfo(bookInfo1)
                    .quantity(3)
                    .build();
            bookRepository.save(book1);
            Book book2 = Book.builder()
                    .bookInfo(bookInfo2)
                    .quantity(1)
                    .build();
            bookRepository.save(book2);

            // when
            List<Book> books = bookRepository.findAll();

            // then
            assertThat(books.size()).isEqualTo(2);
            assertThat(books).contains(book1);
            assertThat(books).contains(book2);
        }

        @Test
        @DisplayName("도서가 없는 경우")
        void findAllBooks_WhenNoBook() {
            //given

            // when
            List<Book> books = bookRepository.findAll();

            // then
            assertThat(books).isEmpty();
        }
    }

    @Test
    @DisplayName("도서 재고 수정 테스트")
    void updateStockAmt() {
        //given
        Book book1 = Book.builder()
                .bookInfo(bookInfo1)
                .quantity(3)
                .build();
        bookRepository.save(book1);

        // when
        book1.setQuantity(10);
        Book updatedBook = bookRepository.save(book1);

        // then
        assertThat(updatedBook.getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("대출 가능한 도서 수량 감소 테스트")
    void decreaseLoanableCnt() {
        //given
        Book book1 = Book.builder()
                .bookInfo(bookInfo1)
                .quantity(3)
                .build();
        bookRepository.save(book1);

        // when
        book1.decreaseLoanableCnt();
        Book updatedBook = bookRepository.save(book1);

        // then
        assertThat(updatedBook.getLoanableCnt()).isEqualTo(2);
    }

    @Test
    @DisplayName("대출 가능한 도서 수량 증가 테스트")
    void increaseLoanableCnt() {
        //given
        Book book2 = Book.builder()
                .bookInfo(bookInfo2)
                .quantity(1)
                .build();
        bookRepository.save(book2);
        book2.decreaseLoanableCnt();
        bookRepository.save(book2);

        // when
        book2.increaseLoanableCnt();
        Book updatedBook = bookRepository.save(book2);

        // then
        assertThat(updatedBook.getLoanableCnt()).isEqualTo(1);
    }

    @Test
    @DisplayName("도서 삭제 테스트")
    void deleteBookTest() {
        //given
        Book book1 = Book.builder()
                .bookInfo(bookInfo1)
                .quantity(3)
                .build();
        bookRepository.save(book1);

        // when
        bookRepository.delete(book1);

        // then
        assertThat(bookRepository.existsById(book1.getId())).isFalse();
        assertThat(bookInfoRepository.existsById(bookInfo1.getIsbn())).isTrue();
    }
}
