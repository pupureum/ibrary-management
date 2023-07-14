package com.plee.library.service.book;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.member.MemberLoanHistory;
import com.plee.library.repository.book.BookRepository;
import com.plee.library.repository.member.MemberLoanHistoryRepository;
import com.plee.library.dto.member.condition.LoanHistorySearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookReturnScheduledService {

    private final MemberLoanHistoryRepository memberLoanHistoryRepository;
    private final BookRepository bookRepository;
//    private final TaskScheduler taskScheduler;
//    private ScheduledFuture<?> scheduledFuture;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정(0시 0분 0초)에 실행
    public void processBookReturnAtMidnight() {
        LocalTime scheduledTime = LocalTime.MIDNIGHT;
        processBookReturn(scheduledTime);
    }

    @Transactional
    public void processBookReturn(LocalTime scheduledTime) {
        LocalDateTime scheduledDateTime = LocalDateTime.of(LocalDate.now(), scheduledTime);
        log.info("processBookReturn scheduled at {}", scheduledDateTime);

        // 요청한 시간 기준으로 연체된 대출 기록 조회
        List<MemberLoanHistory> overDueHistories = memberLoanHistoryRepository.search(LoanHistorySearchCondition.builder()
                        .time(scheduledDateTime.minusDays(7))
                .build());

        if (!overDueHistories.isEmpty()) {
            log.info("overDueHistories exist");
            for (MemberLoanHistory history : overDueHistories) {
                try {
                    Book book = bookRepository.findByBookInfoIsbn(history.getBookInfo().getIsbn())
                            .orElseThrow(() -> new IllegalArgumentException("해당 도서를 찾을 수 없습니다."));
                    book.increaseLoanableCnt();
                    history.setReturnedAt(scheduledDateTime);
                } catch (IllegalArgumentException e) {
                    log.error("도서를 찾을 수 없습니다. ISBN: {}", history.getBookInfo().getIsbn());
                    log.error("반납처리가 되지 않았습니다. MemberLoanHistoryId: {}", history.getId());
                }
            }
                memberLoanHistoryRepository.saveAll(overDueHistories);
            log.info("Returned {} books", overDueHistories.size());
        }

        // 반납되지 않은 대출 기록 중, 반납 기한을 지난 경우 반납 처리
//            if (loanHistory.getReturnedAt() == null && returnDeadline.isBefore(scheduledDateTime)) {
//                loanHistory.setReturnedAt(scheduledDateTime);
//                loanHistoryRepository.save(loanHistory);
//            }
    }
//    public void scheduleBookReturn(LocalTime scheduledTime) {
//        if (scheduledFuture != null) {
//            scheduledFuture.cancel(false);
//        }
//
//        LocalTime currentTime = LocalTime.now();
//        LocalDateTime scheduledDateTime = LocalDateTime.of(LocalDate.now(), scheduledTime);
//        if (scheduledTime.isBefore(currentTime)) {
//            // 만약 입력된 시각이 현재 시각보다 이전인 경우
//            // 다음 날로 스케줄링 시각을 조정
//            scheduledDateTime = scheduledDateTime.plusDays(1);
//        }
//        Instant instant = scheduledDateTime.atZone(ZoneId.systemDefault()).toInstant();
//        scheduledFuture = taskScheduler.schedule(() -> processBookReturn(scheduledTime), instant);
//
////        long delay = Duration.between(currentTime, scheduledTime).toMillis();
////        scheduledFuture = taskScheduler.schedule(() -> processBookReturn(scheduledTime), new Date(System.currentTimeMillis() + delay));
//    }
}
