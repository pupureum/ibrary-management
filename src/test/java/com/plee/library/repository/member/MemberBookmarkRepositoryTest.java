package com.plee.library.repository.member;

import com.plee.library.config.TestJPAConfig;
import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookCategory;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberBookmark;
import com.plee.library.repository.book.BookCategoryRepository;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private BookCategoryRepository bookCategoryRepository;
    @Autowired
    private BookRepository bookRepository;

    private List<Member> members = new ArrayList<>();

    private List<Book> books = new ArrayList<>();

    @BeforeEach
    void setUp() {
        //를 위한 회원 2명 생성
        members = IntStream.rangeClosed(1, 2)
                .mapToObj(i -> Member.builder()
                        .name("회원" + i)
                        .loginId("test" + i + "@gmail.com")
                        .password("test12")
                        .build())
                .map(memberRepository::save)
                .collect(Collectors.toList());

        BookCategory category = BookCategory.builder()
                .categoryName("category")
                .build();
        bookCategoryRepository.save(category);

        //를 위한 도서 2권 생성
        books = IntStream.rangeClosed(1, 2)
                .mapToObj(i -> {
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

                    return Book.builder()
                            .bookInfo(bookInfo)
                            .quantity(1)
                            .category(category)
                            .build();
                })
                .map(bookRepository::save)
                .collect(Collectors.toList());
    }

    @Test
    @DisplayName("도서 찜 등록")
    void save() {
        // given
        Member member = members.get(0);
        Book book = books.get(0);
        MemberBookmark bookmark = MemberBookmark.builder()
                .member(member)
                .book(book)
                .build();
        // when
        MemberBookmark savedMemberBookmark = memberBookmarkRepository.save(bookmark);

        // then
        assertThat(savedMemberBookmark).isNotNull().usingRecursiveComparison().isEqualTo(bookmark);
    }


    @Nested
    @DisplayName("도서 찜 전체 조회")
    public class findBookmarkTest {

        @Test
        @DisplayName("회원별 도서 찜 조회")
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
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).isNotEmpty().hasSize(2).contains(memberBookmark1, memberBookmark2);
        }

        @Test
        @DisplayName("도서별 도서 찜 조회")
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
            assertThat(result).hasSize(2).contains(memberBookmark1, memberBookmark2);
        }
    }

    @Test
    @DisplayName("도서 찜 등록 여부 조회")
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
    @DisplayName("도서 찜 삭제")
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