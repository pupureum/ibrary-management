package com.plee.library.scheduler;

import com.plee.library.service.book.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;

@Slf4j
@EnableScheduling
@RequiredArgsConstructor
@Component
public class BookReturnScheduler {

    private final BookService bookService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정(0시 0분 0초)에 실행
    public void processBookReturnAtMidnight() {
        LocalDateTime scheduledAt = LocalDate.now().atTime(LocalTime.MIDNIGHT);
        log.info("processBookReturn scheduled at {}", scheduledAt);

        try {
            int returnedSize = bookService.processDailyBookReturn(scheduledAt);
            log.info("SUCCESS processBookReturnAtMidnight: {} books returned", returnedSize);
        } catch (Exception e) {
            log.error("ERROR processBookReturnAtMidnight error: {}", e.getMessage());
        }
    }
}
