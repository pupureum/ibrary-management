package com.plee.library.repository.member;

import com.plee.library.config.TestJPAConfig;
import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberBookmark;
import com.plee.library.repository.book.BookInfoRepository;
import com.plee.library.repository.book.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestJPAConfig.class)
@DisplayName("MemberBookmarkRepository 테스트")
class MemberBookmarkRepositoryTest {

    @Autowired
    private MemberBookmarkRepository memberBookmarkRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private BookInfoRepository bookInfoRepository;
    @Autowired
    private BookRepository bookRepository;

    private List<Member> members = new ArrayList<>();

    private List<Book> books = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // 테스트를 위한 회원 2명 생성
        for (int i = 1; i <= 2; i++) {
            Member member = Member.builder()
                    .name("회원" + i)
                    .loginId("test" + i + "@gmail.com")
                    .password("test12")
                    .build();
            memberRepository.save(member);
            members.add(member);
        }
        // 테스트를 위한 도서 2권 생성
        for (int i = 1; i <= 2; i++) {
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
    }

    @Test
    @DisplayName("도서 찜 등록 테스트")
    void save() {
        // given
        Member member = members.get(0);
        Book book = books.get(0);

        // when
        MemberBookmark savedMemberBookmark = memberBookmarkRepository.save(MemberBookmark.builder()
                .member(member)
                .book(book)
                .build());

        // then
        assertThat(savedMemberBookmark).isNotNull();
        assertThat(savedMemberBookmark.getMember()).isEqualTo(member);
        assertThat(savedMemberBookmark.getBook()).isEqualTo(book);
    }


    @Nested
    @DisplayName("도서 찜 전체 조회 테스트")
    public class findBookmarkTest {

        @Test
        @DisplayName("회원별 도서 찜 조회 테스트")
        void findAllByMemberId() {
            // given
            Member member = members.get(0);
            Book book1 = books.get(0);
            MemberBookmark memberBookmark1 = MemberBookmark.builder()
                    .member(member)
                    .book(book1)
                    .build();
            memberBookmarkRepository.save(memberBookmark1);
            Book book2 = books.get(1);
            MemberBookmark memberBookmark2 = MemberBookmark.builder()
                    .member(member)
                    .book(book2)
                    .build();
            memberBookmarkRepository.save(memberBookmark2);

            // when
            Pageable pageable = Pageable.ofSize(5);
            Page<MemberBookmark> result = memberBookmarkRepository.findAllByMemberId(member.getId(), pageable);

            // then
            assertThat(result.getContent()).isNotEmpty();
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).contains(memberBookmark1, memberBookmark2);
        }

        @Test
        @DisplayName("도서별 도서 찜 조회 테스트")
        void findAllByBookId() {
            // given
            Member member1 = members.get(0);
            Book book = books.get(0);
            MemberBookmark memberBookmark1 = MemberBookmark.builder()
                    .member(member1)
                    .book(book)
                    .build();
            memberBookmarkRepository.save(memberBookmark1);
            Member member2 = members.get(1);
            MemberBookmark memberBookmark2 = MemberBookmark.builder()
                    .member(member2)
                    .book(book)
                    .build();
            memberBookmarkRepository.save(memberBookmark2);

            // when
            List<MemberBookmark> result = memberBookmarkRepository.findAllByBookId(book.getId());

            // then
            assertThat(result).isNotEmpty();
            assertThat(result.size()).isEqualTo(2);
            assertThat(result).contains(memberBookmark1, memberBookmark2);
        }
    }

    @Test
    @DisplayName("도서 찜 등록 여부 조회 테스트")
    void existsByMemberIdAndBookIdTest() {
        // given
        Member member = members.get(0);
        Book book = books.get(0);
        MemberBookmark memberBookmark = MemberBookmark.builder()
                .member(member)
                .book(book)
                .build();
        memberBookmarkRepository.save(memberBookmark);

        // when
        assertThat(memberBookmarkRepository.existsByMemberIdAndBookId(member.getId(), book.getId())).isTrue();
    }

    @Test
    @DisplayName("도서 찜 삭제 테스트")
    void deleteByMemberIdAndBookIdTest() {
        // given
        Member member = members.get(0);
        Book book = books.get(0);
        MemberBookmark memberBookmark = MemberBookmark.builder()
                .member(member)
                .book(book)
                .build();
        memberBookmarkRepository.save(memberBookmark);

        // when
        memberBookmarkRepository.deleteByMemberIdAndBookId(member.getId(), book.getId());

        // then
        assertThat(memberBookmarkRepository.existsByMemberIdAndBookId(member.getId(), book.getId())).isFalse();
    }
}