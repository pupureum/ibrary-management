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
import com.plee.library.dto.book.condition.BookSearchCondition;
import com.plee.library.dto.book.request.*;
import com.plee.library.dto.book.response.*;
import com.plee.library.util.message.BookMessage;
import com.plee.library.util.message.MemberMessage;
import com.plee.library.repository.book.BookInfoRepository;
import com.plee.library.repository.book.BookRepository;
import com.plee.library.repository.member.MemberBookmarkRepository;
import com.plee.library.repository.member.MemberLoanHistoryRepository;
import com.plee.library.repository.member.MemberRepository;
import com.plee.library.repository.member.MemberRequestHistoryRepository;
import com.plee.library.dto.member.condition.LoanHistorySearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.plee.library.util.constant.Constants.LOANABLE_BOOK_LIMIT;

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

    /**
     * 도서 입고처리를 위해 저장합니다.
     * 도서 정보와 수량을 받아서 새로운 도서를 저장합니다.
     *
     * @param request 도서 정보와 수량을 담고 있는 요청 객체
     * @throws IllegalStateException 이미 동일한 도서가 존재하는 경우
     */
    @Override
    @Transactional
    public void saveBook(SaveBookRequest request) {
        // 이미 도서가 존재하는 경우
        if (bookRepository.existsByBookInfoIsbn(request.getIsbn())) {
            throw new IllegalStateException(BookMessage.ALREADY_EXIST_BOOK.getMessage());
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

    /**
     * 도서 정보가 존재하는 경우, 승인처리 후 도서 정보를 가져옵니다.
     * 도서 정보가 없다면 생성합니다.
     *
     * @param request 승인 처리할 도서 정보를 담은 요청 객체
     * @return 승인 처리된 도서 정보
     */
    private BookInfo checkBookRequestAlready(SaveBookRequest request) {
        Optional<BookInfo> bookInfo = bookInfoRepository.findById(request.getIsbn());
        if (bookInfo.isPresent()) {
            acceptBookRequestAlready(bookInfo.get());
            return bookInfo.get();
        }
        return bookInfoRepository.save(request.toEntity());
    }

    /**
     * 해당 도서에 대한 다른 회원의 요청이 이미 있는 경우, 승인 처리합니다.
     * 이미 승인처리된 도서가 삭제된 후 재입고 하는경우, 승인처리 되지 않습니다.
     *
     * @param bookInfo 승인 처리할 도서 정보
     */
    private void acceptBookRequestAlready(BookInfo bookInfo) {
        if (memberReqHisRepository.existsByBookInfoIsbnAndIsApprovedFalse(bookInfo.getIsbn())) {
            memberReqHisRepository.approveByBookInfoIsbn(bookInfo.getIsbn());
        }
    }

    /**
     * 회원의 신규 도서 추가 요청을 등록합니다.
     * 이미 등록된 도서거나, 회원이 추가 요청을 한 도서라면 예외 처리합니다.
     *
     * @param request  추가 요청 정보
     * @param memberId 회원 ID
     * @throws IllegalStateException  이미 보유한 도서거나, 회원이 추가 요청을 한 도서인 경우
     * @throws NoSuchElementException 회원 정보를 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public void addNewBookRequest(AddBookRequest request, Long memberId) {
        Member member = findMemberById(memberId);
        String requestIsbn = request.getIsbn();

        // 이미 보유한 도서인 경우
        if (bookRepository.existsByBookInfoIsbn(requestIsbn)) {
            throw new IllegalStateException(BookMessage.ALREADY_EXIST_BOOK.getMessage());
        }

        // 이미 추가 요청한 도서인 경우
        if (memberReqHisRepository.existsByMemberIdAndBookInfoIsbn(member.getId(), requestIsbn)) {
            throw new IllegalStateException(BookMessage.ALREADY_BOOK_REQUEST.getMessage());
        }

        // 다른 회원의 요청 등의 이유로 도서 정보가 존재한다면 해당 정보를 사용, 존재하지 않는다면 도서 정보 생성
        BookInfo bookInfo = bookInfoRepository.findById(requestIsbn)
                .orElseGet(() -> bookInfoRepository.save(request.toEntity()));
        member.addBookRequest(bookInfo, request.getReqReason());
    }

    /**
     * 회원의 도서 대출을 처리합니다.
     *
     * @param bookId   도서 ID
     * @param memberId 회원 ID
     * @throws NoSuchElementException 해당 회원 또는 도서를 찾을 수 없는 경우
     * @throws IllegalStateException  대출 가능한 도서가 없는 경우, 이미 대출 중인 도서인 경우, 대출 가능한 도서의 수를 초과한 경우
     */
    @Override
    @Transactional
    public void loanBook(Long bookId, Long memberId) {
        // 회원 및 도서 조회
        Member member = findMemberById(memberId);
        Book book = findBookById(bookId);

        // 대출 가능한 도서 수량이 없는 경우
        if (book.getLoanableCnt() <= 0) {
            throw new IllegalStateException(BookMessage.CANNOT_LOAN_BOOK.getMessage());
        }

        // 이미 대출한 도서인 경우
        if (memberLoanHisRepository.findByMemberIdAndBookInfoIsbnAndReturnedAtIsNull(member.getId(), book.getBookInfo().getIsbn()).isPresent()) {
            throw new IllegalStateException(BookMessage.ALREADY_LOAN_BOOK.getMessage());
        }

        // 대출 가능한 도서의 수를 초과한 경우
        if (memberLoanHisRepository.countByMemberIdAndReturnedAtIsNull(member.getId()) >= LOANABLE_BOOK_LIMIT) {
            throw new IllegalStateException(BookMessage.MAX_LOAN_BOOK.getMessage());
        }

        member.loanBook(book);
        book.decreaseLoanableCnt();
        log.info("SUCCESS loanBook bookId = {}, loginId = {}", bookId, memberId);
    }

    /**
     * 회원의 도서 반납을 처리합니다.
     *
     * @param request 반납할 도서에 대한 요청 정보
     * @throws NoSuchElementException 회원 정보를 찾을 수 없는 경우, 해당 도서 또는 대출 내역을 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public void returnBook(ReturnBookRequest request, Long memberId) {
        // 도서가 없는 경우 예외 처리
        Book book = bookRepository.findByBookInfoIsbn(request.getBookInfoIsbn())
                .orElseThrow(() -> new NoSuchElementException(BookMessage.NOT_FOUND_BOOK.getMessage()));

        // 대출 내역이 없는 경우 예외 처리
        MemberLoanHistory history = memberLoanHisRepository.findByMemberIdAndBookInfoIsbnAndReturnedAtIsNull(memberId, book.getBookInfo().getId())
                .orElseThrow(() -> new NoSuchElementException(BookMessage.NOT_FOUND_LOAN_HISTORY.getMessage()));

        // 반납 처리 및 대출 가능한 도서 수량 증가
        history.doReturn();
        book.increaseLoanableCnt();
        log.info("SUCCESS returnBook historyId = {}", history.getId());
    }

    /**
     * 스케줄러를 통해 매일 자정 대출 기간이 끝난 도서 반납을 처리합니다.
     *
     * @param scheduledAt 일정 시간
     * @return 처리된 연체된 대출 기록의 수
     * @throws NoSuchElementException 대출 기록에 해당하는 도서가 존재하지 않는경우
     * @throws IllegalStateException  도서의 대출 가능한 수가 올바르지 않은 경우
     */
    @Override
    @Transactional
    public int processDailyBookReturn(LocalDateTime scheduledAt) {
        // 요청된 시간 기준으로 연체된 대출 기록 조회
        List<MemberLoanHistory> overdueHis = memberLoanHisRepository.searchOverdueHistory(LoanHistorySearchCondition.builder()
                .time(scheduledAt)
                .build());

        // 연체된 기록이 있는 경우
        if (!overdueHis.isEmpty()) {
            log.info("overdueHis exist");

            // 중복 도서를 그룹화하여 수량 계산 및 해당 기록 반납 처리
            Map<String, Integer> bookInfoCount = processLoanHistory(overdueHis, scheduledAt);

            // 도서 정보로 반납 처리한 도서 조회
            List<Book> booksToUpdate = bookRepository.findByBookInfoIsbnIn(bookInfoCount.keySet());

            // 반납 처리한 대출 기록에 존재하지 않는 도서가 있는 경우 예외 처리
            validateBookInfoCount(booksToUpdate, bookInfoCount);

            // 반납 처리한 도서의 대출 가능한 수량 증가
            increaseLoanableCnt(booksToUpdate, bookInfoCount);

            memberLoanHisRepository.saveAll(overdueHis);
            bookRepository.saveAll(booksToUpdate);
        }
        return overdueHis.size();
    }

    /**
     * 반납 처리된 도서의 대출 가능한 수량을 증가시킵니다.
     *
     * @param booksToUpdate 반납 처리된 도서 목록
     * @param bookInfoCount 각 도서별 증가시킬 수량
     */
    private void increaseLoanableCnt(List<Book> booksToUpdate, Map<String, Integer> bookInfoCount) {
        for (Book book : booksToUpdate) {
            int loanableCountIncrease = bookInfoCount.getOrDefault(book.getBookInfo().getIsbn(), 0);
            book.increaseLoanableCnt(loanableCountIncrease);
        }
    }

    /**
     * 대출 기록에서의 도서 수와 반납 처리된 도서의 수가 일치하는지 유효성을 검사합니다.
     *
     * @param booksToUpdate 반납 처리된 도서 목록
     * @param bookInfoCount 각 도서별 증가시킬 수량
     * @throws NoSuchElementException 반납 처리된 도서 중 존재하지 않는 도서가 있는 경우
     */
    private void validateBookInfoCount(List<Book> booksToUpdate, Map<String, Integer> bookInfoCount) {
        // 대출 기록에서 가져온 도서 정보와 반납 처리한 도서 정보의 수가 일치하지 않는 경우
        if (bookInfoCount.size() != booksToUpdate.size()) {
            Set<String> foundBooks = booksToUpdate.stream()
                    .map(book -> book.getBookInfo().getIsbn())
                    .collect(Collectors.toSet());

            Set<String> notFoundBooks = bookInfoCount.keySet().stream()
                    .filter(isbn -> !foundBooks.contains(isbn))
                    .collect(Collectors.toSet());
            // book이 존재 하지 않는 isbn값만을 추출하여 메세지에 추가
            throw new NoSuchElementException(notFoundBooks + BookMessage.NOT_FOUND_BOOK.getMessage());
        }
    }

    /**
     * 도서 별 그룹화 및 대출 기록 반납 처리를 수행합니다.
     *
     * @param overdueHis   연체된 대출 기록
     * @param scheduledAt 일정 시간
     * @return 그룹화 한 반납 도서 별 수량
     */
    private Map<String, Integer> processLoanHistory(List<MemberLoanHistory> overdueHis, LocalDateTime scheduledAt) {
        Map<String, Integer> bookInfoCount = new HashMap<>();

        for (MemberLoanHistory history : overdueHis) {
            String bookIsbn = history.getBookInfo().getIsbn();
            bookInfoCount.put(bookIsbn, bookInfoCount.getOrDefault(bookIsbn, 0) + 1);

            // 대출 기록 반납 처리
            history.setReturnedAt(scheduledAt);
        }
        return bookInfoCount;
    }

    /**
     * 회원의 도서 대출 기간을 연장합니다.
     *
     * @param historyId 대출 내역 ID
     * @throws NoSuchElementException 대출 내역을 찾을 수 없는 경우
     * @throws IllegalStateException  대출 기간 연장이 불가능한 경우
     */
    @Override
    @Transactional
    public void renewBook(Long historyId) {
        // 대출 내역 조회
        MemberLoanHistory targetLoanHistory = memberLoanHisRepository.findById(historyId)
                .orElseThrow(() -> new NoSuchElementException(BookMessage.NOT_FOUND_LOAN_HISTORY.getMessage()));

        // 대출중이 아닌 경우
        if (targetLoanHistory.isReturned()) {
            throw new IllegalStateException(BookMessage.ALREADY_RETURN_BOOK.getMessage());
        }

        // 이미 대출 연장을 한 경우
        if (!targetLoanHistory.isRenewable()) {
            throw new IllegalStateException(BookMessage.ALREADY_RENEW_BOOK.getMessage());
        }

        // 반납 처리
        targetLoanHistory.doRenew();
        log.info("SUCCESS renewBook historyId = {}", historyId);
    }

    /**
     * 도서의 수량을 수정합니다.
     *
     * @param bookId  도서 ID
     * @param request 수정할 도서의 정보 (수량)
     * @throws IllegalArgumentException 현재 수량과 같은 수량으로 수정하거나, 대출중인 도서 수보다 적은 수량으로 수정할 경우 예외 발생
     * @throws NoSuchElementException   존재하지 않는 도서인 경우
     */
    @Override
    @Transactional
    public void updateBookQuantity(Long bookId, UpdateBookRequest request) {
        Integer newQuantity = request.getNewQuantity();
        Book book = findBookById(bookId);

        // 현재 수량과 같은 수량으로 수정하려는 경우
        if (newQuantity.equals(book.getQuantity())) {
            throw new IllegalArgumentException(BookMessage.CANNOT_UPDATE_SAME_QUANTITY.getMessage());
        }

        //대출중인 도서 수보다 적은 수량으로 수정하려는 경우
        int loanedCnt = book.getQuantity() - book.getLoanableCnt();
        if (newQuantity < loanedCnt) {
            throw new IllegalArgumentException(BookMessage.CANNOT_UPDATE_QUANTITY.getMessage());
        }

        book.setQuantity(newQuantity);
        log.info("SUCCESS updateBookQuantity Book ID = {}, New quantity = {}", bookId, newQuantity);
    }

    /**
     * 책 정보를 삭제합니다.
     * 대출 이력이나 요청 이력이 없는 경우, 해당 책 정보도 삭제합니다.
     *
     * @param isbn 책 정보의 ISBN(13)
     */
    @Transactional
    public void deleteBookInfo(String isbn) {
        boolean hasLoanHistory = memberLoanHisRepository.existsByBookInfoIsbnAndReturnedAtIsNull(isbn);
        boolean hasRequestHistory = memberReqHisRepository.existsByBookInfoIsbn(isbn);

        if (!hasLoanHistory && !hasRequestHistory) {
            bookInfoRepository.deleteById(isbn);
            log.info("SUCCESS deleteBookInfo Book ISBN = {}", isbn);
        }
    }

    /**
     * 더이상 보유하지 않은 도서를 삭제합니다.
     * 대출 중인 도서는 반납처리 됩니다.
     *
     * @param bookId 도서 ID
     * @throws NoSuchElementException 존재하지 않는 도서인 경우
     */
    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        Book book = findBookById(bookId);

        // 현재 대출중인 도서가 있다면 반납 처리
        if (book.getLoanableCnt() != book.getQuantity()) {
            List<MemberLoanHistory> notReturnedHis = memberLoanHisRepository.searchHistory(LoanHistorySearchCondition.builder()
                    .bookInfoId(book.getBookInfo().getId())
                    .build());

            notReturnedHis.forEach(history -> {
                history.doReturn();
            });
            log.info("SUCCESS return not returned books = {}", notReturnedHis.size());
        }

        // 찜 목록에서 삭제
        List<MemberBookmark> bookmarks = memberBookmarkRepository.findAllByBookId(bookId);
        if (!bookmarks.isEmpty()) {
            memberBookmarkRepository.deleteAll(bookmarks);
            log.info("SUCCESS deleteBook bookmarks = {}", bookmarks.size());
        }

        // 도서 및 도서 정보 삭제
        bookRepository.deleteById(bookId);
        deleteBookInfo(book.getBookInfo().getIsbn());
        log.info("SUCCESS deleteBook Book ID = {}", bookId);
    }

    /**
     * 회원의 도서 찜을 추가합니다.
     *
     * @param memberId 회원 ID
     * @param bookId   도서 ID
     * @throws NoSuchElementException 회원 정보를 찾을 수 없는 경우, 존재하지 않는 도서인 경우,
     * @throws IllegalStateException  이미 찜한 경우
     */
    @Override
    @Transactional
    public void addBookmark(Long memberId, Long bookId) {
        Member member = findMemberById(memberId);
        Book book = findBookById(bookId);

        // 이미 찜한 경우
        if (isAlreadyBookmark(memberId, bookId)) {
            throw new IllegalStateException(BookMessage.ALREADY_BOOKMARK.getMessage());
        }

        member.addBookmark(book);
        log.info("SUCCESS addBookmark member = {}, bookId = {}", member.getLoginId(), bookId);
    }

    /**
     * 이미 찜이 되어있는지 확인합니다.
     *
     * @param memberId 회원 ID
     * @param bookId   도서 ID
     */
    private boolean isAlreadyBookmark(Long memberId, Long bookId) {
        return memberBookmarkRepository.existsByMemberIdAndBookId(memberId, bookId);
    }

    /**
     * 회원의 도서 찜을 해제합니다.
     *
     * @param memberId 회원 ID
     * @param bookId   도서 ID
     * @throws NoSuchElementException 회원 정보를 찾을 수 없는 경우, 존재하지 않는 도서인 경우,
     * @throws IllegalStateException  이미 찜한 경우
     */
    @Override
    @Transactional
    public void removeBookmark(Long memberId, Long bookId) {
        // 존재하지 않는 도서인 경우
        if (!bookRepository.existsById(bookId)) {
            throw new NoSuchElementException(BookMessage.NOT_FOUND_BOOK.getMessage());
        }

        // 찜하지 않은 경우
        if (!isAlreadyBookmark(memberId, bookId)) {
            throw new IllegalStateException(BookMessage.NOT_FOUND_BOOKMARK.getMessage());
        }
        memberBookmarkRepository.deleteByMemberIdAndBookId(memberId, bookId);
        log.info("SUCCESS addBookmark member = {}, bookId = {}", memberId, bookId);
    }

    /**
     * 최근 5일간의 대출 수를 계산하여 리스트로 반환합니다.
     * 날짜에 대출 수가 없는 경우 0으로 처리됩니다.
     *
     * @return 최근 5일간의 대출 수를 담은 LoanStatusResponse 객체
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

    /**
     * 주어진 검색 키워드를 사용하여 부합하는 도서를 검색하고, 페이지네이션 하여 조회합니다.
     * 제목과 저자와 일치하는 정보를 반환합니다.
     * 해당하는 회원의 찜 등록 여부를 포함해 반환합니다.
     *
     * @param request  검색 요청 정보 (키워드)
     * @param memberId 회원 ID
     * @param pageable 페이지 정보
     * @return 검색된 책 정보와 찜 등록 여부 정보를 담은 Page 객체
     */
    public Page<AllBooksMarkInfoResponse> findBySearchKeyword(SearchBookRequest request, Long memberId, Pageable pageable) {
        // 키워드 앞뒤의 공백 제거
        String keyword = request.getKeyword().trim();

        // 부합하는 도서를 검색하고, 페이지네이션하여 조회
        Page<Book> books = bookRepository.search(
                BookSearchCondition.builder()
                        .keyword(keyword)
                        .title(request.isTitle())
                        .author(request.isAuthor())
                        .build(), pageable);

        // 조회된 책들을 AllBooksMarkInfoResponse 객체의 리스트로 변환
        List<AllBooksMarkInfoResponse> response = books.map(book -> mapToAllBooksMarkResponse(book, memberId)).toList();
        return new PageImpl<>(response, pageable, books.getTotalElements());
    }

    /**
     * 보유중인 도서를 생성일 기준 내림차순으로 정렬하여 상위 4권을 조회합니다.
     *
     * @return 최근 입고된 도서의 정보를 담은 BookInfoResponse 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<BookInfoResponse> findNewBooks() {
        // 최근 입고된 도서 4권 조회
        List<Book> books = bookRepository.findTop4ByOrderByCreatedAtDesc();
        return BookInfoResponse.from(books);
    }

    /**
     * 전체 도서를 페이지네이션 하여 최근 도서순으로 조회합니다.
     *
     * @param pageable 페이지 정보
     * @return 전체 도서 정보를 담은 Page 객체
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AllBooksResponse> findAllBooks(Pageable pageable) {
        // 책들을 최신순으로 페이지네이션 하여 조회
        Page<Book> books = bookRepository.findAll(pageable);

        // 조회된 책들을 AllBooksResponse 객체의 리스트로 변환
        List<AllBooksResponse> response = AllBooksResponse.from(books);
        return new PageImpl<>(response, pageable, books.getTotalElements());
    }

    /**
     * 특정 회원의 찜 여부 정보와 전체 도서를 페이지네이션하여 최근 도서순으로 조회합니다.
     *
     * @param memberId 회원의 ID
     * @param pageable 페이지 정보
     * @return 특정 회원의 찜 정보와 도서 정보를 담은 Page 객체
     */
    public Page<AllBooksMarkInfoResponse> findAllBooksWithMark(Long memberId, Pageable pageable) {
        Page<Book> books = bookRepository.findAll(pageable);

        // 조회된 책들을 찜 여부 정보를 포함한 AllBooksMarkResponse 객체의 리스트로 변환
        List<AllBooksMarkInfoResponse> response = books.map(book -> mapToAllBooksMarkResponse(book, memberId)).toList();
        return new PageImpl<>(response, pageable, books.getTotalElements());
    }

    /**
     * 도서 별 회원의 찜 등록 여부 정보를 포함한 AllBooksMarkResponse 객체로 변환합니다.
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
     * 모든 대출 이력을 페이지네이션하여 조회합니다.
     *
     * @param pageable 페이지 정보
     * @return 대출 이력 정보 담은 Page 객체
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AllLoanHistoryResponse> findAllLoanHistory(Pageable pageable) {
        Page<MemberLoanHistory> histories = memberLoanHisRepository.findAll(pageable);

        // 조회된 대출 이력을 AllLoanHistoryResponse 객체의 리스트로 변환
        List<AllLoanHistoryResponse> loanHistories = AllLoanHistoryResponse.from(histories);
        return new PageImpl<>(loanHistories, pageable, histories.getTotalElements());
    }

    /**
     * 특정 회원의 대출 기록을 페이지네이션 하여 최신순으로 조회합니다.
     *
     * @param memberId 회원의 ID
     * @param pageable 페이지 정보
     * @return 회원의 대출 기록을 담은 Page 객체
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
     * 특정 회원의 대출 중인 기록을 대여일로부터 내림차순하여 반환합니다.
     * 최대 3건이 조회됩니다.
     *
     * @param memberId 회원의 ID
     * @return 회원의 대출중인 도서 목록 (Page<LoanHistoryResponse>)
     */
    @Override
    @Transactional(readOnly = true)
    public Page<LoanHistoryResponse> findOnLoanHistory(Long memberId) {
        //대출중인 기록 조회
        List<MemberLoanHistory> histories = memberLoanHisRepository.searchHistory(LoanHistorySearchCondition.builder()
                .memberId(memberId).notReturned(true).build());
        Collections.sort(histories, Comparator.comparing(MemberLoanHistory::getCreatedAt).reversed());

        // 대출 기록을 LoanHistoryResponse 객체의 리스트로 변환
        List<LoanHistoryResponse> response = LoanHistoryResponse.from(histories);

        Pageable pageable = PageRequest.of(0, 5);
        return new PageImpl<>(response, pageable, histories.size());
    }

    /**
     * 특정 회원의 신규 도서 신청 내역을 페이지네이션하여 최신순으로 조회합니다.
     *
     * @param memberId 회원 ID
     * @param pageable 페이지 정보
     * @return 회원의 도서 신청 내역을 담은 Page 객체
     */
    @Override
    @Transactional(readOnly = true)
    public Page<RequestHistoryResponse> findMemberRequestHistory(Long memberId, Pageable pageable) {
        Page<MemberRequestHistory> histories = memberReqHisRepository.findAllByMemberId(memberId, pageable);

        // 요청 기록을 RequestHistoryResponse 객체의 리스트로 변환
        List<RequestHistoryResponse> response = RequestHistoryResponse.from(histories);
        return new PageImpl<>(response, pageable, histories.getTotalElements());
    }

    /**
     * 모든 신규 도서 요청 내역을 페이지네이션하여 최신순으로 조회합니다.
     *
     * @param pageable 페이지 정보
     * @return 신규 도서 요청 내역을 담은 Page 객체
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
        // 키워드가 null이거나 공백일 경우 null 반환
//        if (keyword == null || keyword.isBlank()) {
//            return null;
//        }

        // 네이버 api를 사용하여 키워드로 도서 검색
        SearchApiBookRequest searchReq = new SearchApiBookRequest(keyword);
        return naverBookSearchConfig.searchBook(searchReq);
    }

    /**
     * 특정 회원이 찜한 도서들을 페이지네이션하여 조회합니다.
     *
     * @param memberId 회원 ID
     * @param pageable 페이지 정보
     * @return 찜한 도서들의 정보를 담은 Page 객체
     */
    @Override
    @Transactional(readOnly = true)
    public Page<MarkedBooksResponse> findBookmarked(Long memberId, Pageable pageable) {
        Page<MemberBookmark> bookmarkedBooks = memberBookmarkRepository.findAllByMemberId(memberId, pageable);

        // 조회된 책들을 AllBooksResponse 객체의 리스트로 변환
        List<MarkedBooksResponse> response = MarkedBooksResponse.from(bookmarkedBooks);

        // 변환된 리스트와 페이지 정보를 포함한 Page 객체를 생성하여 반환합니다.
        return new PageImpl<>(response, pageable, bookmarkedBooks.getTotalElements());
    }

    /**
     * 도서 상세 정보와 해당 회원의 대출 중 여부, 찜 등록 여부를 조회합니다.
     *
     * @param memberId 회원 ID
     * @param bookId   도서 ID
     * @return BookDetailResponse 객체
     * @throws NoSuchElementException 도서를 찾을 수 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public BookDetailResponse getBookDetails(Long memberId, Long bookId) {
        Book book = findBookById(bookId);
        boolean isMarked = isBookMarked(memberId, bookId);
        boolean isLoaned = isLoaned(memberId, bookId);

        // 도서 상세 정보와 회원의 대출 중 여부, 찜 등록 여부를 BookDetailResponse 객체로 변환
        return BookDetailResponse.of(book, isLoaned, isMarked);
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
    private boolean isLoaned(Long memberId, Long bookId) {
        Book book = findBookById(bookId);
        return memberLoanHisRepository.findByMemberIdAndBookInfoIsbnAndReturnedAtIsNull(memberId, book.getBookInfo().getIsbn())
                .isPresent();
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
                .orElseThrow(() -> new NoSuchElementException(BookMessage.NOT_FOUND_BOOK.getMessage()));
    }

    /**
     * 회원 ID를 통해 회원을 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 조회된 Member 객체
     * @throws NoSuchElementException 회원을 찾을 수 없을 경우 예외 발생
     */
    @Transactional(readOnly = true)
    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NoSuchElementException(MemberMessage.NOT_FOUND_MEMBER.getMessage()));
    }
}
