package com.plee.library.repository.book;

import com.plee.library.domain.book.Book;
import com.plee.library.dto.book.condition.BookSearchCondition;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.plee.library.domain.book.QBook.book;
import static com.plee.library.domain.book.QBookInfo.bookInfo;

@RequiredArgsConstructor
@Repository
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Book> search(BookSearchCondition condition, Pageable pageable) {
        List<Book> results = queryFactory
                .select(book)
                .from(book)
                .leftJoin(book.bookInfo, bookInfo)
                .where(
                        searchExpression(condition)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(book.count())
                .from(book)
                .leftJoin(book.bookInfo, bookInfo)
                .where(
                        searchExpression(condition)
                );

        // pageable과 results를 확인하여 상황에 따라 count 쿼리를 호출
        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    private BooleanBuilder searchExpression(BookSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();

        builder
                .or(titleLike(condition.isTitle(), condition.getKeyword()))
                .or(authorLike(condition.isAuthor(), condition.getKeyword()))
                .and(categoryEq(condition.getCategoryId()));
        return builder;
    }

    private BooleanExpression titleLike(boolean title, String keyword) {
        return title ? book.bookInfo.title.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression authorLike(boolean author, String keyword) {
        return author ? book.bookInfo.author.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression categoryEq(Long categoryId) {
        return categoryId != null ? book.bookCategory.id.eq(categoryId) : null;
    }
}
