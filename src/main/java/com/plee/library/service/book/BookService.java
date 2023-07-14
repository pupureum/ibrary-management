package com.plee.library.service.book;

import com.plee.library.domain.book.Book;
import com.plee.library.dto.admin.request.UpdateBookRequest;
import com.plee.library.dto.admin.response.AllBooksResponse;
import com.plee.library.dto.admin.response.AllLoanHistoryResponse;
import com.plee.library.dto.admin.response.RequestStatusResponse;
import com.plee.library.dto.admin.response.LoanStatusResponse;
import com.plee.library.dto.book.request.*;
import com.plee.library.dto.book.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    void saveBook(SaveBookRequest request);

    void addNewBookRequest(AddBookRequest request, Long memberId);

    void loanBook(Long bookId, Long memberId);

    void renewBook(Long historyId);

    void returnBook(ReturnBookRequest request, Long memberId);

    void updateBookQuantity(Long bookId, UpdateBookRequest request);

    void deleteBook(Long bookId);

    void addBookmark(Long memberId, Long bookId);

    void removeBookmark(Long memberId, Long bookId);

    LoanStatusResponse calculateDailyLoanCounts();

    Page<AllBooksMarkInfoResponse> findBySearchKeyword(SearchBookRequest request, Long memberId, Pageable pageable);

    List<BookInfoResponse> findNewBooks();

    BookDetailResponse getBookDetails(Long memberId, Long bookId);

    Page<AllBooksResponse> findAllBooks(Pageable pageable);

    Page<AllBooksMarkInfoResponse> findAllBooksWithMark(Long memberId, Pageable pageable);

    Page<LoanHistoryResponse> findLoanHistory(Long memberId, Pageable pageable);

    Page<LoanHistoryResponse> findOnLoanHistory(Long memberId);

    Page<AllLoanHistoryResponse> findAllLoanHistory(Pageable pageable);

    Page<RequestHistoryResponse> findMemberRequestHistory(Long memberId, Pageable pageable);

    SearchBookResponse findBySearchApi(String keyword);

    Page<RequestStatusResponse> findAllNewBookReqHistory(Pageable pageable);

    Page<MarkedBooksResponse> findBookmarked(Long memberId, Pageable pageable);

    Book findBookById(Long bookId);

    boolean isBookMarked(Long memberId, Long bookId);
}