package com.plee.library.repository.book;

import com.plee.library.config.TestJPAConfig;
import com.plee.library.domain.book.BookInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestJPAConfig.class)
@DisplayName("BookInfoRepository 테스트")
class BookInfoRepositoryTest {
    @Autowired
    private BookInfoRepository bookInfoRepository;

    @Nested
    @DisplayName("도서 정보 생성")
    public class SaveBookInfoTest {
        @Test
        @DisplayName("도서 정보 생성 성공")
        void save() {
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

            // when
            BookInfo savedBookInfo = bookInfoRepository.save(bookInfo);

            // then
            assertThat(savedBookInfo).isNotNull();
            assertThat(savedBookInfo.getIsbn()).isEqualTo(bookInfo.getIsbn());
            assertThat(savedBookInfo.getTitle()).isEqualTo(bookInfo.getTitle());
            assertThat(savedBookInfo.getAuthor()).isEqualTo(bookInfo.getAuthor());
            assertThat(savedBookInfo.getPublisher()).isEqualTo(bookInfo.getPublisher());
            assertThat(savedBookInfo.getImage()).isEqualTo(bookInfo.getImage());
            assertThat(savedBookInfo.getDescription()).isEqualTo(bookInfo.getDescription());
            assertThat(savedBookInfo.getPubDate()).isEqualTo(bookInfo.getPubDate());
        }

        @Test
        @DisplayName("실패: 중복된 ISBN 값이 존재하는 경우")
        void saveFailWithNotUniqueIsbn() {
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

            BookInfo dupBookInfo = BookInfo.builder()
                    .isbn("9788994492032")
                    .title("HTTP 완벽 가이드")
                    .author("안슈 아가왈")
                    .publisher("인사이트")
                    .image("https://shopping-phinf.pstatic.net/main_3246114/32461143685.20230606105115.jpg")
                    .description("책 소개입니다")
                    .pubDate("20141215")
                    .build();

            // when, then
            assertThatThrownBy(() -> bookInfoRepository.save(dupBookInfo))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        @DisplayName("실패: ISBN 값이 null 인 경우")
        void saveFailWithNullIsbn() {
            // given
            BookInfo bookInfo = BookInfo.builder()
                    .isbn(null)
                    .title("Java의 정석")
                    .build();

            // when, then
            assertThatThrownBy(() -> bookInfoRepository.save(bookInfo))
                    .isInstanceOf(JpaSystemException.class);
        }

        @Test
        @DisplayName("실패: title 값이 null 인 경우")
        void saveFailWithNullTitle() {
            // given
            BookInfo bookInfo = BookInfo.builder()
                    .isbn("9788994492032")
                    .title(null)
                    .build();

            // when, then
            assertThatThrownBy(() -> bookInfoRepository.saveAndFlush(bookInfo))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("ISBN 값으로 도서 정보 조회")
    public class FindBookInfoByIsbnTest {
        @Test
        @DisplayName("조회 성공")
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
            BookInfo foundBookInfo = bookInfoRepository.findById(bookInfo.getIsbn()).orElse(null);

            // then
            assertThat(foundBookInfo).isNotNull();
            assertThat(foundBookInfo.getIsbn()).isEqualTo(bookInfo.getIsbn());
            assertThat(foundBookInfo.getTitle()).isEqualTo(bookInfo.getTitle());
            assertThat(foundBookInfo.getAuthor()).isEqualTo(bookInfo.getAuthor());
            assertThat(foundBookInfo.getPublisher()).isEqualTo(bookInfo.getPublisher());
            assertThat(foundBookInfo.getImage()).isEqualTo(bookInfo.getImage());
            assertThat(foundBookInfo.getDescription()).isEqualTo(bookInfo.getDescription());
            assertThat(foundBookInfo.getPubDate()).isEqualTo(bookInfo.getPubDate());
        }

        @Test
        @DisplayName("없는 ISBN 값으로 조회하는 경우")
        void findWithNotExistIsbn() {
            // given
            String isbn = "9788994492032";

            // when
            BookInfo foundBookInfo = bookInfoRepository.findById(isbn).orElse(null);

            // then
            assertThat(foundBookInfo).isNull();
        }
    }

    @Nested
    @DisplayName("도서 정보 전체 조회")
    public class FindAllTest {
        @Test
        @DisplayName("전체 조회 성공")
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
            assertThat(bookInfos).hasSize(2).contains(bookInfo1, bookInfo2);
        }

        @Test
        @DisplayName("도서 정보가 없는 경우")
        void findAllBookInfoWhenNoBookInfo() {
            // when
            List<BookInfo> bookInfos = bookInfoRepository.findAll();

            // then
            assertThat(bookInfos).isEmpty();
        }
    }

    @Test
    @DisplayName("특정 도서 저자 수정")
    void updateTest() {
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
        assertThat(updatedBookInfo).isNotNull();
        assertThat(updatedBookInfo.getAuthor()).isEqualTo(newAuthor);
    }

    @Test
    @Transactional
    @DisplayName("도서 정보 삭제")
    void deleteBookInfoTest() {
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
        bookInfoRepository.deleteById(bookInfo.getId());

        // then
        assertThat(bookInfoRepository.existsById(bookInfo.getId())).isFalse();
    }
}