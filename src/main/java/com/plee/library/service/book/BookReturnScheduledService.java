package com.plee.library.service.book;

import com.plee.library.domain.book.Book;
import com.plee.library.domain.member.MemberLoanHistory;
import com.plee.library.repository.book.BookRepository;
import com.plee.library.repository.member.MemberLoanHistoryRepository;
import com.plee.library.dto.member.condition.LoanHistorySearchCondition;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

import static com.plee.library.util.constant.Constants.LOAN_PERIOD;

@Slf4j
@EnableScheduling
@RequiredArgsConstructor
@Component
public class BookReturnScheduledService {

    private final BookService bookService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정(0시 0분 0초)에 실행
    public void processBookReturnAtMidnight() {
        LocalDateTime scheduledAt = LocalDate.now().atTime(LocalTime.MIDNIGHT);
        log.info("processBookReturn scheduled at {}", scheduledAt);
        try {
            int size = bookService.processDailyBookReturn(scheduledAt);
            log.info("SUCCESS processBookReturnAtMidnight: {} books returned", size);
        } catch (Exception e) {
            log.error("processBookReturnAtMidnight error: {}", e.getMessage());
        }
    }
}
