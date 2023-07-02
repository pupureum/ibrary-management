//package com.plee.library.service.book;
//
//import com.plee.library.domain.book.Book;
//import com.plee.library.domain.member.Member;
//import com.plee.library.domain.member.MemberLoanHistory;
//import com.plee.library.dto.book.request.LoanBookRequest;
//import com.plee.library.dto.book.request.ReturnBookRequest;
//import com.plee.library.dto.book.response.AllBooksResponse;
//import com.plee.library.dto.book.response.LoanHistoryResponse;
//import com.plee.library.repository.book.BookRepository;
//import com.plee.library.repository.member.MemberRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class BookLoanServiceImpl implements BookLoanService {
//
//    private final BookRepository bookRepository;
//    private final MemberRepository memberRepository;
//
//    @Override
//    @Transactional
//    public void loanBook(Long bookId, String loginId) {
//        Book book = bookRepository.findById(bookId)
//                .orElseThrow(() -> new IllegalArgumentException("not found book: " + bookId));
//        if (book.getLoanableCnt() <= 0) {
//            throw new IllegalArgumentException("not loanable book: " + bookId);
//        }
//
//        Member member = memberRepository.findByLoginId(loginId)
//                .orElseThrow(() -> new IllegalArgumentException("not found member: " + loginId));
//        member.loanBook(book.getBookInfo());
//        book.decreaseLoanableCnt();
//    }
//
//    @Override
//    public List<LoanHistoryResponse> findLoanHistory(String loginId) {
//        Member member = memberRepository.findByLoginId(loginId)
//                .orElseThrow(() -> new IllegalArgumentException("not found member: " + loginId));
//        List<MemberLoanHistory> histories = member.getMemberLoanHistories();
//        return histories.stream()
//                .map(h -> LoanHistoryResponse.builder()
//                        .id(h.getId())
//                        .bookInfo(h.getBookInfo())
//                        .loanedAt(h.getLoanedAt())
//                        .returnedAt(h.getReturnedAt())
//                        .build())
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public void returnBook(Long bookId, String loginId) {
//
//    }
//
//    @Override
//    public void renewBook(Long bookId, String loginId) {
//
//    }
//}
