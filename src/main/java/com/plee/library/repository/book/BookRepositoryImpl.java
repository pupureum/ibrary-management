package com.plee.library.repository.book;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.QBook;
import com.plee.library.domain.book.QBookInfo;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    private final QBook book = QBook.book;
    private final QBookInfo bookInfo = QBookInfo.bookInfo;

    @Override
    public Page<Book> findBooksWithSearchValue(String searchValue, Pageable pageable) {
        BooleanExpression titleOrAuthorContains = book.bookInfo.title.containsIgnoreCase(searchValue)
                .or(book.bookInfo.author.containsIgnoreCase(searchValue));

        List<Book> results = queryFactory
                .select(book)
                .from(book)
                .leftJoin(book.bookInfo, bookInfo)
                .where(titleOrAuthorContains)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(book.count())
                .from(book)
                .leftJoin(book.bookInfo, bookInfo)
                .where(titleOrAuthorContains);

        // pageable과 results를 확인하여 상황에 따라 count 쿼리를 호출
        return PageableExecutionUtils.getPage(results, pageable, countQuery::fetchOne);
    }
}
