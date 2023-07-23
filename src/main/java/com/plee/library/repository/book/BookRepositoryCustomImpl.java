package com.plee.library.repository.book;

import com.plee.library.domain.book.Book;
import com.plee.library.dto.book.condition.BookSearchCondition;
import com.plee.library.util.OrderByNull;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                .orderBy(sortBook(pageable))
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

        // results 와 pageable 를 확인하여 상황에 따라 count 쿼리 호출
        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }

    /**
     * 도서를 정렬하기 위한 OrderSpecifier 를 생성하는 메서드입니다.
     *
     * @param pageable Pageable 객체
     * @return OrderSpecifier 객체로서 적절한 정렬 조건을 포함
     */
    private OrderSpecifier<?> sortBook(Pageable pageable) {
        // Pageable 객체에 정렬 조건이 없는 경우
        if (pageable.getSort().isEmpty()) {
            return OrderByNull.getDefault();
        }

        // 정렬 조건 처리
        for (Sort.Order order : pageable.getSort()) {
            Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case "createdAt" -> {
                    return new OrderSpecifier<>(direction, book.createdAt);
                }
            }
        }
        return OrderByNull.getDefault();
    }

    /**
     * 검색 조건을 기반으로 BooleanBuilder 를 생성하는 메서드입니다.
     *
     * @param condition 검색 조건
     * @return 검색 조건에 따라 구성된 BooleanBuilder 객체를 반환
     */
    private BooleanBuilder searchExpression(BookSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();

        return builder
                .or(titleLike(condition.isTitle(), condition.getKeyword()))
                .or(authorLike(condition.isAuthor(), condition.getKeyword()))
                .and(categoryEq(condition.getCategoryId()));
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
