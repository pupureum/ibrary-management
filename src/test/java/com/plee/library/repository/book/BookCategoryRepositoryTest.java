package com.plee.library.repository.book;

import com.plee.library.config.TestJPAConfig;
import com.plee.library.domain.book.BookCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(TestJPAConfig.class)
@DisplayName("BookCategoryRepository 테스트")
public class BookCategoryRepositoryTest {
    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    @Nested
    @DisplayName("도서 카테고리 생성")
    public class SaveBookCategoryTest {
        @Test
        @DisplayName("카테고리 생성 성공")
        void saveSuccess() {
            // given
            BookCategory bookCategory = BookCategory.builder()
                    .categoryName("IT/프로그래밍")
                    .build();

            // when
            BookCategory savedBookCategory = bookCategoryRepository.save(bookCategory);

            // then
            assertThat(savedBookCategory).isNotNull().usingRecursiveComparison().isEqualTo(bookCategory);
        }

        @Test
        @DisplayName("실패: 이미 동일한 카테고리 이름 존재")
        void saveFail() {
            // given
            BookCategory bookCategory = BookCategory.builder()
                    .categoryName("IT/프로그래밍")
                    .build();
            bookCategoryRepository.save(bookCategory);

            BookCategory dupBookCategory = BookCategory.builder()
                    .categoryName("IT/프로그래밍")
                    .build();

            // when, then
            assertThatThrownBy(() -> bookCategoryRepository.save(dupBookCategory)).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("특정 카테고리 조회")
    public class FindCategoryTest {
        @Test
        @DisplayName("조회 성공")
        void findById() {
            // given
            BookCategory bookCategory = BookCategory.builder()
                    .categoryName("IT/프로그래밍")
                    .build();
            BookCategory savedCategory = bookCategoryRepository.save(bookCategory);

            // when
            Long bookId = savedCategory.getId();
            BookCategory foundCategory = bookCategoryRepository.findById(bookId).orElse(null);

            // then
            assertThat(foundCategory).isNotNull().usingRecursiveComparison().isEqualTo(bookCategory);
        }

        @Test
        @DisplayName("없는 카테고리 id로 조회한 경우")
        void findByIdNotExits() {
            // given
            Long categoryId = 3L;

            // when
            BookCategory category = bookCategoryRepository.findById(categoryId).orElse(null);

            // then
            assertThat(category).isNull();
        }
    }

    @Nested
    @DisplayName("카테고리 전체 조회")
    public class FindAllTest {
        @Test
        @DisplayName("전체 조회 성공")
        void findAllBooksCategory() {
            // given
            BookCategory bookCategory1 = BookCategory.builder()
                    .categoryName("IT/프로그래밍")
                    .build();
            bookCategoryRepository.save(bookCategory1);
            BookCategory bookCategory2 = BookCategory.builder()
                    .categoryName("소설")
                    .build();
            bookCategoryRepository.save(bookCategory2);

            // when
            List<BookCategory> categories = bookCategoryRepository.findAll();

            // then
            assertThat(categories).hasSize(2).contains(bookCategory1, bookCategory2);
        }

        @Test
        @DisplayName("카테고리 없는 경우")
        void findAllBooksCategoryWhenNoCategory() {
            // given

            // when
            List<BookCategory> categories = bookCategoryRepository.findAll();

            // then
            assertThat(categories).isEmpty();
        }
    }
}
