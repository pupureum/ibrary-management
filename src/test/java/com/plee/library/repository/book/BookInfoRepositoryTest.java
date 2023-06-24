package com.plee.library.repository.book;

import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.dto.book.request.AddBookRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("BookInfoRepository 테스트")
class BookInfoRepositoryTest {
    @Autowired
    private BookInfoRepository bookInfoRepository;

    @Test
    @DisplayName("도서 정보 생성 테스트")
    void createBookInfo() {
        // given
//        BookInfo bookInfo = new AddBookRequest("9788994492032", "Java의 정석", "남궁성", "도우출판",
//                "https://shopping-phinf.pstatic.net/main_3246668/32466681076.20230622071100.jpg", "책 소개입니다", "20160201", "reason").toEntity();
        BookInfo bookInfo = BookInfo.builder()
                .isbn("9788994492032")
                .title("Java의 정석")
                .author("남궁성")
                .publisher("도우출판")
                .image("https://shopping-phinf.pstatic.net/main_3246668/32466681076.20230622071100.jpg")
                .description("책 소개입니다")
                .pubDate("20160201")
                .build();

        // when
        BookInfo savedBookInfo = bookInfoRepository.save(bookInfo);

        // then
        assertNotNull(savedBookInfo);
        assertEquals(bookInfo, savedBookInfo);
        assertEquals(bookInfo.getIsbn(), savedBookInfo.getIsbn());
        assertEquals(bookInfo.getTitle(), savedBookInfo.getTitle());
        assertEquals(bookInfo.getAuthor(), savedBookInfo.getAuthor());
        assertEquals(bookInfo.getPublisher(), savedBookInfo.getPublisher());
        assertEquals(bookInfo.getImage(), savedBookInfo.getImage());
        assertEquals(bookInfo.getDescription(), savedBookInfo.getDescription());
        assertEquals(bookInfo.getPubDate(), savedBookInfo.getPubDate());
    }

    @Test
    @DisplayName("isbn으로 도서 정보 조회 테스트")
    void findBookInfoByIsbn() {
        // given
        BookInfo bookInfo = BookInfo.builder()
                .isbn("9788994492032")
                .title("Java의 정석")
                .author("남궁성")
                .publisher("도우출판")
                .image("https://shopping-phinf.pstatic.net/main_3246668/32466681076.20230622071100.jpg")
                .description("책 소개입니다")
                .pubDate("20160201")
                .build();
        bookInfoRepository.save(bookInfo);

        // when
        BookInfo foundBookInfo = bookInfoRepository.findById(bookInfo.getIsbn())
                .orElse(null);

        // then
        assertNotNull(foundBookInfo);
        assertEquals(bookInfo, foundBookInfo);
        assertEquals(bookInfo.getIsbn(), foundBookInfo.getIsbn());
        assertEquals(bookInfo.getTitle(), foundBookInfo.getTitle());
        assertEquals(bookInfo.getAuthor(), foundBookInfo.getAuthor());
        assertEquals(bookInfo.getPublisher(), foundBookInfo.getPublisher());
        assertEquals(bookInfo.getImage(), foundBookInfo.getImage());
        assertEquals(bookInfo.getDescription(), foundBookInfo.getDescription());
        assertEquals(bookInfo.getPubDate(), foundBookInfo.getPubDate());
    }

    @Test
    @DisplayName("저장된 도서 정보 전체 조회 테스트")
    void findAllBooksInfo() {
        // given
        BookInfo bookInfo1 = BookInfo.builder()
                .isbn("9788994492032")
                .title("Java의 정석")
                .author("남궁성")
                .publisher("도우출판")
                .image("https://shopping-phinf.pstatic.net/main_3246668/32466681076.20230622071100.jpg")
                .description("책 소개입니다")
                .pubDate("20160201")
                .build();
        bookInfoRepository.save(bookInfo1);

        BookInfo bookInfo2 = BookInfo.builder()
                .isbn("9788966261208")
                .title("HTTP 완벽 가이드")
                .author("안슈 아가왈")
                .publisher("인사이트")
                .image("https://shopping-phinf.pstatic.net/main_3246114/32461143685.20230606105115.jpg")
                .description("책 소개입니다")
                .pubDate("20141215")
                .build();
        bookInfoRepository.save(bookInfo2);

        // when
        List<BookInfo> bookInfos = bookInfoRepository.findAll();

        // then
        assertEquals(2, bookInfos.size());
        assertTrue(bookInfos.contains(bookInfo1));
        assertTrue(bookInfos.contains(bookInfo2));
    }

    @Test
    @DisplayName("특정 도서 저자 수정 테스트")
    void updateBookInfo() {
        // given
        BookInfo bookInfo = BookInfo.builder()
                .isbn("9788966261208")
                .title("HTTP 완벽 가이드")
                .author("안슈 아가왈")
                .publisher("인사이트")
                .image("https://shopping-phinf.pstatic.net/main_3246114/32461143685.20230606105115.jpg")
                .description("책 소개입니다")
                .pubDate("20141215")
                .build();
        bookInfoRepository.save(bookInfo);

        // when
        String newAuthor = "안슈 아가왈2";
        bookInfo.updateAuthor(newAuthor);
        BookInfo updatedBookInfo = bookInfoRepository.save(bookInfo);

        // then
        assertEquals(newAuthor, updatedBookInfo.getAuthor());
    }

    @Test
    @DisplayName("특정 도서 정보 삭제 테스트")
    void deleteBookInfo() {
        // given
        BookInfo bookInfo = BookInfo.builder()
                .isbn("9788966261208")
                .title("HTTP 완벽 가이드")
                .author("안슈 아가왈")
                .publisher("인사이트")
                .image("https://shopping-phinf.pstatic.net/main_3246114/32461143685.20230606105115.jpg")
                .description("책 소개입니다")
                .pubDate("20141215")
                .build();
        bookInfoRepository.save(bookInfo);

        // when
        bookInfoRepository.delete(bookInfo);

        // then
        assertFalse(bookInfoRepository.existsById(bookInfo.getIsbn()));
    }
}