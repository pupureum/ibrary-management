package com.plee.library.repository.book;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookInfo;
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

    BookInfo bookInfo1;
    BookInfo bookInfo2;

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

    @Test
    @DisplayName("도서 생성 테스트")
    void createBook() {
        // given
        // 아래 형식은 서비스단으로 옮기기 TODO
//        SaveBookRequest request = new SaveBookRequest("9788994492032", "Java의 정석", "남궁성", "도우출판",
//                "https://shopping-phinf.pstatic.net/main_3246668/32466681076.20230622071100.jpg", "책 소개입니다", "20160201", 5);
        Book book = Book.builder()
                .bookInfo(bookInfo1)
                .stock_amt(5)
                .build();

        // when
        Book savedBook = bookRepository.save(book);

        // then
        assertNotNull(savedBook);
        assertEquals(book.getId(), savedBook.getId());
        assertEquals(book.getBookInfo(), savedBook.getBookInfo());
        assertEquals(book.getStock_amt(), savedBook.getStock_amt());
        assertEquals(book.getLoanable_cnt(), savedBook.getLoanable_cnt());
    }

    @Test
    @DisplayName("Id로 저장된 도서 조회 테스트")
    void findBookById() {
        //given
        Book book1 = Book.builder()
                .bookInfo(bookInfo1)
                .stock_amt(3)
                .build();
        bookRepository.save(book1);
        Book book2 = Book.builder()
                .bookInfo(bookInfo2)
                .stock_amt(1)
                .build();
        bookRepository.save(book2);

        // when
        Book findBook1 = bookRepository.findById(book1.getId()).orElse(null);

        // then
        assertNotNull(findBook1);
        assertEquals(book1.getId(), findBook1.getId());
        assertEquals(book1.getBookInfo(), findBook1.getBookInfo());
        assertEquals(book1.getStock_amt(), findBook1.getStock_amt());
        assertEquals(book1.getLoanable_cnt(), findBook1.getLoanable_cnt());
    }

    @Test
    @DisplayName("보유한 도서 전체 조회 테스트")
    void findAllBooks() {
        //given
        Book book1 = Book.builder()
                .bookInfo(bookInfo1)
                .stock_amt(3)
                .build();
        bookRepository.save(book1);
        Book book2 = Book.builder()
                .bookInfo(bookInfo2)
                .stock_amt(1)
                .build();
        bookRepository.save(book2);

        // when
        List<Book> books = bookRepository.findAll();

        // then
        assertEquals(2, books.size());
        assertTrue(books.contains(book1));
        assertTrue(books.contains(book2));
    }

    @Test
    @DisplayName("도서 재고 수정 테스트")
    void updateStockAmt() {
        //given
        Book book1 = Book.builder()
                .bookInfo(bookInfo1)
                .stock_amt(3)
                .build();
        bookRepository.save(book1);

        // when
        book1.updateStockAmt(10);
        Book updatedBook = bookRepository.save(book1);

        // then
        assertEquals(10, updatedBook.getStock_amt());
    }

    @Test
    @DisplayName("대출 가능한 도서 수량 감소 테스트")
    void decreaseLoanableCnt() {
        //given
        Book book1 = Book.builder()
                .bookInfo(bookInfo1)
                .stock_amt(3)
                .build();
        bookRepository.save(book1);

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
        Book book2 = Book.builder()
                .bookInfo(bookInfo2)
                .stock_amt(1)
                .build();
        bookRepository.save(book2);
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
        //given
        Book book1 = Book.builder()
                .bookInfo(bookInfo1)
                .stock_amt(3)
                .build();
        bookRepository.save(book1);

        // when
        bookRepository.delete(book1);

        // then
        assertFalse(bookRepository.existsById(book1.getId()));
    }
}