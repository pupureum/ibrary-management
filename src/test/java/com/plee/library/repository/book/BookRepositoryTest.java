package com.plee.library.repository.book;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.dto.book.request.SaveBookRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("BookInfoRepository 테스트")
class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookInfoRepository bookInfoRepository;

    @Test
    @DisplayName("도서 생성 테스트")
    void createBook() {
        // given
        // 아래 형식은 서비스단으로 옮기기 TODO
//        SaveBookRequest request = new SaveBookRequest("9788994492032", "Java의 정석", "남궁성", "도우출판",
//                "https://shopping-phinf.pstatic.net/main_3246668/32466681076.20230622071100.jpg", "책 소개입니다", "20160201", 5);
        BookInfo bookInfo = BookInfo.builder()
                .isbn("9788994492032")
                .title("Java의 정석")
                .author("남궁성")
                .publisher("도우출판")
                .image("https://shopping-phinf.pstatic.net/main_3246668/32466681076.20230622071100.jpg")
                .description("책 소개입니다")
                .pubDate("20160201")
                .build();
        BookInfo savedBookInfo = bookInfoRepository.save(bookInfo);

        Book book = Book.builder()
                .bookInfo(savedBookInfo)
                .stock_amt(5)
                .build();

        // when
        Book savedBook = bookRepository.save(book);

        // then
        assertNotNull(savedBook);
        assertEquals(book.getId(), savedBook.getId());
        assertEquals(book.getBookInfo(), savedBook.getBookInfo());
        assertEquals(book.getStock_amt(), savedBook.getStock_amt());

        assertEquals(book, savedBook);
    }

    @Nested
    @DisplayName("생성되어있는 도서를 기반으로 테스트")
    class BookTest {
        private Book book1;
        private Book book2;

        @BeforeEach
        void createBooks() {
            //given
//            SaveBookRequest request1 = new SaveBookRequest("9788994492032", "Java의 정석", "남궁성", "도우출판",
//                    "https://shopping-phinf.pstatic.net/main_3246668/32466681076.20230622071100.jpg", "책 소개입니다", "20160201", 3);
//            SaveBookRequest request2 = new SaveBookRequest("9788966261208", "HTTP 완벽 가이드", "안슈 아가왈", "인사이트",
//                    "https://shopping-phinf.pstatic.net/main_3246114/32461143685.20230606105115.jpg", "책 소개입니다", "20141215", 1);
            BookInfo bookInfo = BookInfo.builder()
                    .isbn("9788994492032")
                    .title("Java의 정석")
                    .author("남궁성")
                    .publisher("도우출판")
                    .image("https://shopping-phinf.pstatic.net/main_3246668/32466681076.20230622071100.jpg")
                    .description("책 소개입니다")
                    .pubDate("20160201")
                    .build();
            BookInfo savedBookInfo1 = bookInfoRepository.save(bookInfo);
            book1 = Book.builder()
                    .bookInfo(savedBookInfo1)
                    .stock_amt(3)
                    .build();
            bookRepository.save(book1);

            BookInfo bookInfo2 = BookInfo.builder()
                    .isbn("9788966261208")
                    .title("HTTP 완벽 가이드")
                    .author("안슈 아가왈")
                    .publisher("인사이트")
                    .image("https://shopping-phinf.pstatic.net/main_3246114/32461143685.20230606105115.jpg")
                    .description("책 소개입니다")
                    .pubDate("20141215")
                    .build();
            BookInfo savedBookInfo2 = bookInfoRepository.save(bookInfo2);
            book2 = Book.builder()
                    .bookInfo(savedBookInfo2)
                    .stock_amt(1)
                    .build();
            bookRepository.save(book2);
        }

        @Test
        @DisplayName("Id로 저장된 도서 조회 테스트")
        void findBookById() {
            // when
            Book findBook1 = bookRepository.findById(book1.getId()).orElse(null);

            // then
            assertNotNull(findBook1);
            assertEquals(book1.getId(), findBook1.getId());
            assertEquals(book1.getBookInfo(), findBook1.getBookInfo());
            assertEquals(book1.getStock_amt(), findBook1.getStock_amt());
        }

        @Test
        @DisplayName("보유한 도서 전체 조회 테스트")
        void findAllBooks() {
            // when
            List<Book> books = bookRepository.findAll();

            // then
            assertEquals(2, books.size());
            assertTrue(books.contains(book1));
            assertTrue(books.contains(book2));
        }

        @Test
        @DisplayName("도서 재고 수정 테스트")
        public void updateStockAmt() {
            // when
            book1.updateStockAmt(10);
            book2.updateStockAmt(5);
            Book updatedBook = bookRepository.save(book1);
            Book updatedBook2 = bookRepository.save(book2);

            // then
            assertEquals(10, updatedBook.getStock_amt());
            assertEquals(5, updatedBook2.getStock_amt());
        }

        @Test
        @DisplayName("대출 가능한 도서 수량 감소 테스트")
        void decreaseLoanableCnt() {
            // when
            book1.decreaseLoanableCnt();
            Book updatedBook = bookRepository.save(book1);

            // then
            assertEquals(2, updatedBook.getLoanable_cnt());
        }

        @Test
        @DisplayName("대출 가능한 도서 수량 증가 테스트")
        void increaseLoanableCnt() {
            //given
            book2.decreaseLoanableCnt();
            bookRepository.save(book2);

            // when
            book2.increaseLoanableCnt();
            Book updatedBook = bookRepository.save(book2);

            // then
            assertEquals(1, updatedBook.getLoanable_cnt());
        }

        @Test
        @DisplayName("도서 삭제 테스트")
        void deleteBook() {
            // when
            bookRepository.delete(book1);

            // then
            Book findBook = bookRepository.findById(book1.getId())
                    .orElse(null);

            // then
            assertNull(findBook);
        }
    }


}