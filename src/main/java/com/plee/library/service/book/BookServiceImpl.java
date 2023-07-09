package com.plee.library.service.book;

import com.plee.library.config.NaverBookSearchConfig;
import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberBookmark;
import com.plee.library.domain.member.MemberLoanHistory;
import com.plee.library.domain.member.MemberRequestHistory;
import com.plee.library.dto.admin.request.UpdateBookRequest;
import com.plee.library.dto.admin.response.AllBooksResponse;
import com.plee.library.dto.admin.response.AllLoanHistoryResponse;
import com.plee.library.dto.admin.response.LoanStatusResponse;
import com.plee.library.dto.admin.response.RequestStatusResponse;
import com.plee.library.dto.book.request.*;
import com.plee.library.dto.book.response.*;
import com.plee.library.exception.message.BookError;
import com.plee.library.exception.message.MemberError;
import com.plee.library.repository.book.BookInfoRepository;
import com.plee.library.repository.book.BookRepository;
import com.plee.library.repository.member.MemberBookmarkRepository;
import com.plee.library.repository.member.MemberLoanHistoryRepository;
import com.plee.library.repository.member.MemberRepository;
import com.plee.library.repository.member.MemberRequestHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookInfoRepository bookInfoRepository;
    private final MemberRequestHistoryRepository memberReqHisRepository;
    private final MemberBookmarkRepository memberBookmarkRepository;
    private final MemberLoanHistoryRepository memberLoanHisRepository;
    private final MemberRepository memberRepository;
    private final NaverBookSearchConfig naverBookSearchConfig;
    private final int LOANABLE_BOOK_LIMIT = 3;

    /**
     * 새로운 도서 추가 요청을 처리합니다.
     * 이미 등록된 도서거나, 회원이 추가 요청을 한 도서라면 예외 처리합니다.
     *
     * @param request  추가 요청 정보
     * @param memberId 회원 ID
     * @throws CustomException 이미 등록된 도서인 경우, 등록된 도서인 경우
     */
    @Override
    @Transactional
    public void addNewBookRequest(AddBookRequest request, Long memberId) {
        String requestIsbn = request.getIsbn();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException(MemberError.NOT_FOUND_MEMBER.getMessage()));

        // 소장한 도서인 경우
        if (bookRepository.existsByBookInfoIsbn(requestIsbn)) {
            throw new IllegalStateException(BookError.ALREADY_EXIST_BOOK.getMessage());
        }

        // 이미 추가 요청한 도서인 경우
        if (memberReqHisRepository.existsByMemberIdAndBookInfoIsbn(memberId, requestIsbn)) {
            throw new IllegalStateException(BookError.ALREADY_BOOK_REQUEST.getMessage());
        }

        // 다른 회원의 요청 등의 이유로 도서 정보가 존재한다면 해당 정보를 사용, 존재하지 않는다면 도서 정보 생성
        BookInfo bookInfo = bookInfoRepository.findById(requestIsbn)
                .orElseGet(() -> bookInfoRepository.save(request.toEntity()));
        member.addBookRequest(bookInfo, request.getReqReason());
    }

    /**
     * 도서를 저장하는 메서드입니다.
     * 도서 정보와 수량을 받아서 새로운 도서를 저장합니다.
     *
     * @param request 도서 정보와 수량을 담고 있는 요청 객체
     * @throws IllegalStateException 이미 동일한 도서가 존재하는 경우
     */
    @Override
    @Transactional
    public void saveBook(SaveBookRequest request) {
        if (bookRepository.existsByBookInfoIsbn(request.getIsbn())) {
            throw new IllegalStateException(BookError.ALREADY_EXIST_BOOK.getMessage());
        }

        // 도서 정보 존재 여부에 따라, 도서 정보를 저장하거나 이미 존재하는 도서 정보를 사용
        BookInfo bookInfo = checkBookRequestAlready(request);
        Book book = Book.builder()
                .bookInfo(bookInfo)
                .quantity(request.getQuantity())
                .build();
        bookRepository.save(book);
        log.info("SUCCESS saveBook bookId = {}", book.getId());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public BookInfo checkBookRequestAlready(SaveBookRequest request) {
        Optional<BookInfo> bookInfo = bookInfoRepository.findById(request.getIsbn());
        if (bookInfo.isPresent()) { //TODO bookInfo는 있지만, memberRequestHistory가 없어도 예외가 아닌경우도 생각 필요
            acceptBookRequestAlready(bookInfo.get());
            return bookInfo.get();
        }
        return bookInfoRepository.save(request.toEntity());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void acceptBookRequestAlready(BookInfo bookInfo) {
        // 해당 도서를 신청한 회원들이 있는 경우, 승인 처리
        if (memberReqHisRepository.existsByBookInfoIsbnAndIsApprovedFalse(bookInfo.getIsbn())) {
            memberReqHisRepository.approveByBookInfoIsbn(bookInfo.getIsbn());
        }
    }

    /**
     * 도서 대출을 처리하는 메서드입니다.
     *
     * @param bookId   도서 ID
     * @param memberId 회원 ID
     * @throws NoSuchElementException   해당 회원 또는 도서를 찾을 수 없는 경우
     * @throws IllegalArgumentException 대출 가능한 도서가 없는 경우
     * @throws IllegalStateException    이미 대출 중인 도서인 경우, 대출 가능한 도서의 수를 초과한 경우
     */
    @Override
    @Transactional
    public void loanBook(Long bookId, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException(MemberError.NOT_FOUND_MEMBER.getMessage()));
        Book book = findBookById(bookId);

        // 대출 가능한 도서 수량이 없는 경우
        if (book.getLoanableCnt() <= 0) {
            throw new IllegalArgumentException(BookError.CANNOT_LOAN_BOOK.getMessage());
        }

        // 이미 대출한 도서인 경우
        if (memberLoanHisRepository.existsByMemberIdAndBookInfoIsbnAndReturnedAtIsNull(member.getId(), book.getBookInfo().getIsbn())) {
            throw new IllegalStateException(BookError.ALREADY_LOAN_BOOK.getMessage());
        }

        // 대출 가능한 도서의 수를 초과한 경우
        if (memberLoanHisRepository.countByMemberIdAndReturnedAtIsNull(member.getId()) >= LOANABLE_BOOK_LIMIT) {
            throw new IllegalStateException(BookError.MAX_LOAN_BOOK.getMessage());
        }

        member.loanBook(book);
        book.decreaseLoanableCnt();
        log.info("SUCCESS loanBook bookId = {}, loginId = {}", bookId, memberId);
    }

    /**
     * 도서 반납을 처리하는 메서드입니다.
     *
     * @param request 반납할 도서에 대한 요청 정보
     * @throws NoSuchElementException 해당 도서 또는 대출 내역을 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public void returnBook(ReturnBookRequest request, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException(MemberError.NOT_FOUND_MEMBER.getMessage()));
        Book book = bookRepository.findByBookInfoIsbn(request.getBookInfoIsbn())
                .orElseThrow(() -> new NoSuchElementException(BookError.NOT_FOUND_BOOK.getMessage()));

        member.returnBook(book.getBookInfo());
        book.increaseLoanableCnt();
        log.info("SUCCESS returnBook historyId = {}", request.getHistoryId());
    }

    /**
     * 도서 대출 기간을 연장하는 메서드입니다.
     *
     * @param historyId 대출 내역 ID
     * @throws NoSuchElementException 대출 내역을 찾을 수 없는 경우
     * @throws IllegalStateException  대출 기간 연장이 불가능한 경우
     */
    @Override
    @Transactional
    public void renewBook(Long historyId) {
        MemberLoanHistory targetLoanHistory = memberLoanHisRepository.findById(historyId)
                .orElseThrow(() -> new NoSuchElementException(BookError.NOT_FOUND_LOAN_HIS.getMessage()));

        // 대출중이 아닌 경우
        if (targetLoanHistory.isReturned()) {
            throw new IllegalStateException(BookError.ALREADY_RETURN_BOOK.getMessage());
        }

        // 이미 대출 연장을 한 경우
        if (!targetLoanHistory.isRenewable()) {
            throw new IllegalStateException(BookError.ALREADY_RENEW_BOOK.getMessage());
        }

        targetLoanHistory.doRenew();
        log.info("SUCCESS renewBook historyId = {}", historyId);
    }

    /**
     * 최근 5일간의 대출 수를 계산하여 리스트로 반환합니다.
     * 날짜에 대출 수가 없는 경우 0으로 처리됩니다.
     *
     * @return 최근 5일간의 대출 수 LoanStatusResponse 객체
     */
    @Override
    @Transactional(readOnly = true)
    public LoanStatusResponse calculateDailyLoanCounts() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(4);
        Map<LocalDate, Integer> dailyLoanCounts = new LinkedHashMap<>();

        // 대출 수를 날짜별로 계산
        List<Object[]> data = memberLoanHisRepository.countGroupByCreatedAtRange(startDate, endDate);

        //시작 날짜부터 마지막까지의 날짜 범위를 순회하며, 날짜와 해당 날짜에 대출된 도서의 수를 Map에 추가
        startDate.datesUntil(endDate.plusDays(1)).forEach(date -> {
            Optional<Object[]> matchingRow = data.stream()
                    .filter(row -> ((java.sql.Date) row[0]).toLocalDate().isEqual(date))
                    .findFirst();

            if (matchingRow.isPresent()) {
                dailyLoanCounts.put(date, ((Long) matchingRow.get()[1]).intValue());
            } else { // 대출 수가 없는 경우 0으로 처리
                dailyLoanCounts.put(date, 0);
            }
        });

        log.info("SUCCESS calculateDailyLoanCounts");
        return new LoanStatusResponse(dailyLoanCounts);
    }

    @Override
    @Transactional
    public void addBookmark(Long memberId, Long bookId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException(MemberError.NOT_FOUND_MEMBER.getMessage()));
        Book book = findBookById(bookId);

        // 이미 찜한 경우
        if (isAlreadyBookmark(memberId, bookId)) {
            throw new IllegalStateException(BookError.ALREADY_BOOKMARK.getMessage());
        }

        member.addBookmark(book);
        log.info("SUCCESS addBookmark member = {}, bookId = {}", member.getLoginId(), bookId);
    }

    private boolean isAlreadyBookmark(Long memberId, Long bookId) {
        return memberBookmarkRepository.existsByMemberIdAndBookId(memberId, bookId);
    }

    @Override
    @Transactional
    public void removeBookmark(Long memberId, Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new NoSuchElementException(BookError.NOT_FOUND_BOOK.getMessage());
        }

        // 찜하지 않은 경우
        if (!isAlreadyBookmark(memberId, bookId)) {
            throw new IllegalStateException(BookError.NOT_FOUND_BOOKMARK.getMessage());
        }
        memberBookmarkRepository.deleteByMemberIdAndBookId(memberId, bookId);
        log.info("SUCCESS addBookmark member = {}, bookId = {}", memberId, bookId);
    }

    /**
     * 모든 대출 이력을 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 대출 상태 페이지 (LoanStatusResponse 객체의 리스트)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AllLoanHistoryResponse> findAllLoanHistory(Pageable pageable) {
        Page<MemberLoanHistory> histories = memberLoanHisRepository.findAll(pageable);
        // 조회된 대출 이력을 AllLoanHistoryResponse 객체로 변환
        List<AllLoanHistoryResponse> loanHistories = histories.stream()
                .map(h -> AllLoanHistoryResponse.builder()
                        .id(h.getId())
                        .member(h.getMember())
                        .bookInfo(h.getBookInfo())
                        .isRenew(h.isRenew())
                        .loanedAt(h.getCreatedAt().toLocalDate())
                        .returnedAt(Optional.ofNullable(h.getReturnedAt()).map(LocalDateTime::toLocalDate).orElse(null))
                        .build())
                .collect(Collectors.toList());
        return new PageImpl<>(loanHistories, pageable, histories.getTotalElements());
    }

    /**
     * 최근에 입고된 도서 4권을 조회하는 메서드입니다.
     * 입고된 도서를 생성일 기준 내림차순으로 정렬하여 상위 4권을 조회합니다.
     *
     * @return 최근 입고된 도서의 정보를 담은 BookInfoResponse 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookInfoResponse> findNewBooks() {
        // 최근 입고된 도서 4권 조회
        List<Book> books = bookRepository.findTop4ByOrderByCreatedAtDesc();
        return books.stream()
                .map(book -> BookInfoResponse.builder()
                        .image(book.getBookInfo().getImage())
                        .title(book.getBookInfo().getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 도서 상세 정보와 해당 회원의 대출 중 여부, 찜 등록 여부를 조회합니다.
     *
     * @param memberId 회원 ID
     * @param bookId 도서 ID
     * @return BookDetailResponse 객체
     * @throws NoSuchElementException 도서를 찾을 수 없는 경우
     */
    @Override
    public BookDetailResponse getBookDetails(Long memberId, Long bookId) {
        Book book = findBookById(bookId);
        boolean isMarked = isBookMarked(memberId, bookId);
        boolean isLoaned = isLoaned(memberId, bookId);

        // 도서 상세 정보와 회원의 대출 중 여부, 찜 등록 여부를 BookDetailResponse로 변환
        return BookDetailResponse.from(book, isLoaned, isMarked);
    }

    /**
     * 전체 도서를 최근 도서순으로 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 전체 도서 목록 페이지 (AllBooksResponse 객체의 리스트)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AllBooksResponse> findAllBooks(Pageable pageable) {
        // 책들을 최신순으로 Pagination 하여 조회
        Page<Book> books = bookRepository.findAll(pageable);

        // 조회된 책들을 AllBooksResponse 객체로 변환
        List<AllBooksResponse> response = books.stream()
                .map(book -> AllBooksResponse.builder()
                        .id(book.getId())
                        .quantity(book.getQuantity())
                        .loanableCnt(book.getLoanableCnt())
                        .bookInfo(book.getBookInfo())
                        .build())
                .collect(Collectors.toList());
        return new PageImpl<>(response, pageable, books.getTotalElements());
    }

    /**
     * 특정 회원의 찜 여부 정보와 전체 도서를 최근 도서순으로 조회합니다.
     *
     * @param memberId 회원의 ID
     * @param pageable 페이징 정보
     * @return 전체 도서 목록 페이지 (AllBooksMarkInfoResponse 객체의 리스트)
     */
    public Page<AllBooksMarkInfoResponse> findAllBooksWithMark(Long memberId, Pageable pageable) {
        // 책들을 최신순으로 Pagination 하여 조회
        Page<Book> books = bookRepository.findAll(pageable);

        // 조회된 책들을 AllBooksMarkResponse 객체로 변환
        List<AllBooksMarkInfoResponse> response = books.map(book -> mapToAllBooksMarkResponse(book, memberId)).toList();
        return new PageImpl<>(response, pageable, books.getTotalElements());
    }

    /**
     * 주어진 Book 객체와 회원 ID를 사용하여 AllBooksMarkResponse 객체로 변환합니다.
     *
     * @param book     도서 객체
     * @param memberId 회원 ID
     * @return AllBooksMarkResponse 객체
     */
    private AllBooksMarkInfoResponse mapToAllBooksMarkResponse(Book book, Long memberId) {
        return AllBooksMarkInfoResponse.builder()
                .id(book.getId())
                .quantity(book.getQuantity())
                .loanableCnt(book.getLoanableCnt())
                .bookInfo(book.getBookInfo())
                .isMarked(isBookMarked(memberId, book.getId()))
                .build();
    }

    /**
     * 주어진 검색 키워드를 사용하여 부합하는 도서를 검색하고, 페이징합니다.
     * 해당하는 회원의 찜 등록 여부를 포함해 반환합니다.
     *
     * @param request  검색 요청 정보 (키워드)
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 검색된 책 목록 페이지와 찜 등록 여부 (AllBooksMarkInfoResponse 객체의 리스트)
     */
    public Page<AllBooksMarkInfoResponse> findBySearchKeyword(SearchBookRequest request, Long memberId, Pageable pageable) {
        // 키워드 앞뒤의 공백 제거
        String keyword = request.getKeyword().trim();
        Page<Book> books = bookRepository.findBooksWithSearchValue(keyword, pageable);

        // 조회된 책들을 AllBooksMarkInfoResponse 객체로 변환
        List<AllBooksMarkInfoResponse> response = books.map(book -> mapToAllBooksMarkResponse(book, memberId)).toList();
        return new PageImpl<>(response, pageable, books.getTotalElements());
    }

    /**
     * 특정 회원의 대출 기록을 최신순으로 조회합니다.
     *
     * @param memberId 회원의 ID
     * @param pageable 페이징 정보
     * @return 회원의 대출 기록 페이지 (LoanHistoryResponse 객체의 리스트)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<LoanHistoryResponse> findLoanHistory(Long memberId, Pageable pageable) {
        Page<MemberLoanHistory> histories = memberLoanHisRepository.findAllByMemberId(memberId, pageable);
        // 대출 기록을 LoanHistoryResponse 객체의 리스트로 변환
        List<LoanHistoryResponse> response = LoanHistoryResponse.from(histories.getContent());
        return new PageImpl<>(response, pageable, histories.getTotalElements());
    }

    /**
     * 특정 회원의 대출 중인 기록을 조회합니다.
     *
     * @param memberId 회원의 ID
     * @return 회원의 대출중인 도서 목록 (LoanHistoryResponse 객체의 리스트)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<LoanHistoryResponse> findOnLoanHistory(Long memberId) {
        Pageable pageable = PageRequest.of(0, 5, DESC, "createdAt");
        List<MemberLoanHistory> histories = memberLoanHisRepository.findByMemberIdAndReturnedAtIsNull(memberId);
        // 대출 기록을 LoanHistoryResponse 객체의 리스트로 변환
        List<LoanHistoryResponse> response = LoanHistoryResponse.from(histories);
        return new PageImpl<>(response, pageable, histories.size());
    }

    /**
     * 특정 회원의 신규 도서 신청 내역을 최신순으로 조회합니다.
     *
     * @param memberId 회원 ID
     * @param pageable 페이징 정보
     * @return 회원의 도서 신청 내역 페이지 (RequestHistoryResponse 객체의 리스트)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RequestHistoryResponse> findMemberRequestHistory(Long memberId, Pageable pageable) {
        // 회원의 신규 도서 요청 기록을 최신순으로 Pagination 하여 조회
        Page<MemberRequestHistory> histories = memberReqHisRepository.findAllByMemberId(memberId, pageable);

        // 요청 기록을 RequestHistoryResponse 객체의 리스트로 변환
        List<RequestHistoryResponse> response = RequestHistoryResponse.from(histories);
        return new PageImpl<>(response, pageable, histories.getTotalElements());
    }

    /**
     * 모든 신규 도서 요청 내역을 최신순으로 조회합니다.
     *
     * @param pageable 페이징 정보
     * @return 신규 도서 요청 내역 페이지 (RequestStatusResponse 객체의 리스트)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RequestStatusResponse> findAllNewBookReqHistory(Pageable pageable) {
        Page<MemberRequestHistory> histories = memberReqHisRepository.findAll(pageable);

        // 조회된 요청들을 RequestStatusResponse 객체의 리스트로 변환
        List<RequestStatusResponse> response = RequestStatusResponse.from(histories);
        return new PageImpl<>(response, pageable, histories.getTotalElements());
    }


    /**
     * 네이버 도서 검색 API를 통해 주어진 키워드로 책을 검색하고, 검색 결과를 반환합니다.
     *
     * @param keyword 검색할 키워드
     * @return 검색 결과를 담은 SearchBookResponse 객체
     */
    @Override
    public SearchBookResponse findBySearchApi(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        // 네이버 api를 사용하여 키워드로 도서 검색
        SearchApiBookRequest searchReq = new SearchApiBookRequest(keyword);
        return naverBookSearchConfig.searchBook(searchReq);
    }

    /**
     * 도서의 수량을 수정합니다.
     *
     * @param bookId  도서 ID
     * @param request 수정할 도서의 정보 (수량)
     * @throws IllegalArgumentException 현재 수량과 같은 수량으로 수정하거나, 대출중인 도서 수보다 적은 수량으로 수정할 경우 예외 발생
     */
    @Override
    @Transactional
    public void updateBookQuantity(Long bookId, UpdateBookRequest request) {
        Integer newQuantity = request.getNewQuantity();
        Book book = findBookById(bookId);

        // 현재 수량과 같은 수량으로 수정하려는 경우
        if (newQuantity.equals(book.getQuantity())) {
            throw new IllegalArgumentException(BookError.CANNOT_UPDATE_SAME_QUANTITY.getMessage());
        }

        //대출중인 도서 수보다 적은 수량으로 수정하려는 경우
        int loanedCnt = book.getQuantity() - book.getLoanableCnt();
        if (newQuantity < loanedCnt) {
            throw new IllegalArgumentException(BookError.CANNOT_UPDATE_QUANTITY.getMessage());
        }

        book.setQuantity(newQuantity);
        log.info("SUCCESS updateBookQuantity Book ID = {}, New quantity = {}", bookId, newQuantity);
    }

    /**
     * 도서를 삭제하는 메서드입니다.
     * 대출 중인 도서는 삭제할 수 없습니다.
     *
     * @param bookId 도서 ID
     * @throws NoSuchElementException 존재하지 않는 도서인 경우 발생하는 예외
     * @throws IllegalStateException  대출 중인 도서를 삭제하려고 할 때 발생하는 예외
     */
    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        Book book = findBookById(bookId);

        // 현재 대출중인 도서인지 확인
        if (book.getLoanableCnt() != book.getQuantity()) {
            throw new IllegalStateException(BookError.CANNOT_DELETE_BOOK.getMessage());
        }

        // 찜 목록에서 삭제
        List<MemberBookmark> bookmarks = memberBookmarkRepository.findAllByBookId(bookId);
        memberBookmarkRepository.deleteAll(bookmarks);
        log.info("SUCCESS deleteBook bookmarks = {}", bookmarks.size());

        // 도서 및 도서 정보 삭제
        bookRepository.deleteById(bookId);
        deleteBookInfo(book.getBookInfo().getIsbn());
        log.info("SUCCESS deleteBook Book ID = {}", bookId);
    }

    /**
     * 책 정보를 삭제합니다.
     * 대출 이력이나 요청 이력이 없는 경우, 해당 책 정보도 삭제합니다.
     *
     * @param isbn 책 정보의 ISBN(13)
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteBookInfo(String isbn) {
        boolean hasLoanHistory = memberLoanHisRepository.existsByBookInfoIsbnAndReturnedAtIsNull(isbn);
        boolean hasRequestHistory = memberReqHisRepository.existsByBookInfoIsbn(isbn);

        if (!hasLoanHistory || !hasRequestHistory) {
            bookInfoRepository.deleteById(isbn);
        }
        log.info("SUCCESS deleteBookInfo Book ISBN = {}", isbn);
    }

    @Override
    public Page<MarkedBooksResponse> findLikeBooks(Long memberId, Pageable pageable) {
        // 책들을 최신순으로 Pagination 하여 조회
        Page<MemberBookmark> bookmarkedBooks = memberBookmarkRepository.findAllByMemberId(memberId, pageable);

        // 조회된 책들을 AllBooksResponse 객체의 리스트로 변환
        List<MarkedBooksResponse> response = bookmarkedBooks.stream()
                .map(book -> MarkedBooksResponse.builder()
                        .id(book.getId())
                        .book(book.getBook())
                        .build())
                .collect(Collectors.toList());
        return new PageImpl<>(response, pageable, bookmarkedBooks.getTotalElements());
    }

    /**
     * 주어진 bookId로 책을 조회합니다.
     *
     * @param bookId 도서 ID
     * @return 조회된 Book 객체
     * @throws NoSuchElementException 도서를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public Book findBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new NoSuchElementException(BookError.NOT_FOUND_BOOK.getMessage()));
    }

    /**
     * 특정 회원이 주어진 도서를 찜 등록 했는지 여부를 확인합니다.
     *
     * @param memberId 회원 ID
     * @param bookId   도서 ID
     * @return 찜 등록 여부
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isBookMarked(Long memberId, Long bookId) {
        return memberBookmarkRepository.existsByMemberIdAndBookId(memberId, bookId);
    }

    /**
     * 특정 회원이 주어진 도서를 대출 중인지 여부를 확인합니다.
     *
     * @param memberId 회원 ID
     * @param bookId   도서 ID
     * @return 대출 중 여부
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isLoaned(Long memberId, Long bookId) {
        Book book = findBookById(bookId);
        return memberLoanHisRepository.existsByMemberIdAndBookInfoIsbnAndReturnedAtIsNull(memberId, book.getBookInfo().getIsbn());
    }
}
