package com.plee.library.service.book;

import com.plee.library.config.NaverBookSearchConfig;
import com.plee.library.domain.book.Book;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.member.Member;
import com.plee.library.domain.member.MemberLoanHistory;
import com.plee.library.domain.member.MemberRequestHistory;
import com.plee.library.dto.admin.response.AllBookRequestResponse;
import com.plee.library.dto.book.request.*;
import com.plee.library.dto.book.response.*;
import com.plee.library.exception.CustomException;
import com.plee.library.exception.code.BookErrorCode;
import com.plee.library.exception.code.MemberErrorCode;
import com.plee.library.repository.book.BookInfoRepository;
import com.plee.library.repository.book.BookRepository;
import com.plee.library.repository.member.MemberBookMarkRepository;
import com.plee.library.repository.member.MemberLoanHistoryRepository;
import com.plee.library.repository.member.MemberRepository;
import com.plee.library.repository.member.MemberRequestHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookInfoRepository bookInfoRepository;
    private final MemberRequestHistoryRepository memberReqHisRepository;
    private final MemberBookMarkRepository memberBookmarkRepository;
    private final MemberLoanHistoryRepository memberLoanHisRepository;
    private final MemberRepository memberRepository;
    private final NaverBookSearchConfig naverBookSearchConfig;

    private final int LOANABLE_BOOK_LIMIT = 3;

    @Override
    @Transactional
    public void addNewBookRequest(AddBookRequest request, String loginId) {
        // 이미 추가 요청을 한 경우 예외 처리
        //TODO member ID 받는걸로 바꾸기
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.NOT_FOUND_MEMBER));
        if (bookRepository.existsByBookInfoIsbn(request.getIsbn())) {
            throw new CustomException(BookErrorCode.ALREADY_EXIST_BOOK);
        }
        if (memberReqHisRepository.existsByMemberIdAndBookInfoIsbn(member.getId(), request.getIsbn())) {
            throw new CustomException(BookErrorCode.ALREADY_EXIST_BOOK_REQUEST);
        }
        //다른 사용자의 요청 등의 이유로 bookInfo 존재 여부 판단
        BookInfo bookInfo = bookInfoRepository.findById(request.getIsbn())
                .orElseGet(() -> bookInfoRepository.save(request.toEntity()));
        member.addBookRequest(bookInfo, request.getReqReason());
    }

    @Override
    @Transactional
    public void saveBook(SaveBookRequest request) {
        if (bookRepository.findByBookInfoIsbn(request.getIsbn()).isPresent()) {
            throw new CustomException(BookErrorCode.ALREADY_EXIST_BOOK);
        }
        // 사용자의 신규 도서 신청으로 인한 bookInfo 존재 여부 판단
        BookInfo bookInfo = checkBookRequestAlready(request);
        Book book = Book.builder()
                .bookInfo(bookInfo)
                .quantity(request.getQuantity())
                .build();
        bookRepository.save(book);
    }

    private BookInfo checkBookRequestAlready(SaveBookRequest request) {
        Optional<BookInfo> bookInfo = bookInfoRepository.findById(request.getIsbn());
        if (bookInfo.isPresent()) { //TODO bookInfo는 있지만, memberRequestHistory가 없어도 예외가 아닌경우도 생각 필요
            acceptBookRequestAlready(bookInfo.get());
            return bookInfo.get();
        }
        return bookInfoRepository.save(request.toEntity());
    }

    private void acceptBookRequestAlready(BookInfo bookInfo) {
        if (memberReqHisRepository.existsByBookInfoIsbnAndIsApprovedFalse(bookInfo.getIsbn())) {
            memberReqHisRepository.approveByBookInfoIsbn(bookInfo.getIsbn());
        }
    }

    @Override
    @Transactional
    public void loanBook(Long bookId, String loginId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("해당 도서를 찾을 수 없습니다."));
        if (book.getLoanableCnt() <= 0) {
            throw new IllegalArgumentException("대출 가능한 도서가 없습니다." + bookId);
        }
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        if (memberLoanHisRepository.existsByMemberIdAndBookInfoIsbnAndReturnedAtIsNull(member.getId(), book.getBookInfo().getIsbn())) {
            throw new IllegalArgumentException("이미 대출중인 도서입니다.");
        }
        if (memberLoanHisRepository.countByMemberIdAndReturnedAtIsNull(member.getId()) >= LOANABLE_BOOK_LIMIT) {
            throw new IllegalArgumentException("대출 가능한 도서의 수를 초과하였습니다.");
        }
        member.loanBook(book.getBookInfo());
        book.decreaseLoanableCnt();
    }

    @Override
    @Transactional
    public void returnBook(ReturnBookRequest request) {
        log.info("returnBook historyId : {}", request.getHistoryId());
        log.info("returnBook bookINfoIsbn : {}", request.getBookInfoIsbn());
        Book book = bookRepository.findByBookInfoIsbn(request.getBookInfoIsbn())
                .orElseThrow(() -> new IllegalArgumentException("해당 도서를 찾을 수 없습니다."));
        MemberLoanHistory targetLoanHistory = memberLoanHisRepository.findByIdAndReturnedAtIsNull(request.getHistoryId())
                .orElseThrow(() -> new IllegalArgumentException("대출 내역이 없습니다."));
        targetLoanHistory.doReturn();
        book.increaseLoanableCnt();
    }

    @Override
    @Transactional
    public void renewBook(Long historyId) {
        MemberLoanHistory targetLoanHistory = memberLoanHisRepository.findByIdAndReturnedAtIsNull(historyId)
                .orElseThrow(() -> new IllegalArgumentException("대출 내역이 없습니다."));
        if (!targetLoanHistory.isRenewable()) {
            throw new IllegalArgumentException("대출 기간 연장은 1회만 가능합니다.");
        }
        targetLoanHistory.doRenew();
    }

    @Override
    public List<BookInfoResponse> findNewBooks() {
        List<Book> books = bookRepository.findTop4ByOrderByCreatedAtDesc();
        return books.stream()
                .map(book -> BookInfoResponse.builder()
                        .image(book.getBookInfo().getImage())
                        .title(book.getBookInfo().getTitle())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<AllBooksResponse> findAllBooks() {
        List<Book> books = bookRepository.findAllByOrderByCreatedAtDesc();

        return books.stream()
                .map(book -> AllBooksResponse.builder()
                        .id(book.getId())
                        .quantity(book.getQuantity())
                        .loanableCnt(book.getLoanableCnt())
                        .bookInfo(book.getBookInfo())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanHistoryResponse> findLoanHistory(String loginId) {
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        // TODO loginID 말고, ID 받아오도록 수정 필요!
        List<MemberLoanHistory> histories = memberLoanHisRepository.findAllByMemberIdOrderByCreatedAtDesc(member.getId());
        return histories.stream()
                .map(h -> LoanHistoryResponse.builder()
                        .id(h.getId())
                        .bookInfo(h.getBookInfo())
                        .isRenew(h.isRenew())
                        .loanedAt(h.getCreatedAt().toLocalDate())
                        .returnedAt(Optional.ofNullable(h.getReturnedAt()).map(LocalDateTime::toLocalDate).orElse(null))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestHistoryResponse> findMemberRequestHistory(String loginId) {
        //member의 Id를 받아오도록 바꾸기! TODO
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        List<MemberRequestHistory> histories = memberReqHisRepository.findAllByMemberIdOrderByCreatedAtDesc(member.getId());
        return histories.stream()
                .map(h -> RequestHistoryResponse.builder()
                        .id(h.getId())
                        .bookInfo(h.getBookInfo())
                        .requestReason(h.getRequestReason())
                        .isApproved(h.isApproved())
                        .requestedAt(h.getCreatedAt().toLocalDate())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<AllBookRequestResponse> findAllMemberRequestHistory() {
        return memberReqHisRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(h -> AllBookRequestResponse.builder()
                        .id(h.getId())
                        .member(h.getMember())
                        .bookInfo(h.getBookInfo())
                        .requestReason(h.getRequestReason())
                        .isApproved(h.isApproved())
                        .requestedAt(h.getCreatedAt().toLocalDate())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Book findBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new CustomException(BookErrorCode.NOT_FOUND_BOOK));
    }

    @Override
    public SearchBookResponse findBySearchApi(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        SearchBookRequest searchReq = new SearchBookRequest();
        searchReq.setKeyword(keyword);
        return naverBookSearchConfig.searchBook(searchReq);
    }

    @Override
    @Transactional
    public void updateBookQuantity(Long bookId, Integer newQuantity) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(BookErrorCode.NOT_FOUND_BOOK));
        // 이미 대출중인 도서 수보다 적게 수정하려는 경우, 수정할 수 없도록 예외 발생
        int loanedCnt = book.getQuantity() - book.getLoanableCnt();
        if (newQuantity < loanedCnt) {
            throw new CustomException(BookErrorCode.CANNOT_UPDATE_QUANTITY);
        }
        book.setQuantity(newQuantity);
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(BookErrorCode.NOT_FOUND_BOOK));
        // 대출중인 도서는 삭제할 수 없도록 예외 발생
        if (book.getLoanableCnt() != book.getQuantity()) {
            throw new CustomException(BookErrorCode.CANNOT_DELETE_BOOK);
        }
        // TODO 책 삭제 시, 대출 기록이나, 신규도서 요청내역에 존재하지 않는 경우, 책 정보도 삭제되도록 하기
        bookRepository.deleteById(bookId);
    }

    @Override
    public boolean isBookMarked(Long bookId, String loginId) {
        return false;
    }

    @Override
    public boolean isLoaned(Long bookId, String loginId) {
        //TODO loginId -> memberID 값으로 바꾸기
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 책입니다."));
        return memberLoanHisRepository.existsByMemberIdAndBookInfoIsbnAndReturnedAtIsNull(member.getId(), book.getBookInfo().getIsbn());
    }
}

