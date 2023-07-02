package com.plee.library.domain.member;

import com.plee.library.domain.book.BookInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Entity
@Table(name = "member_request_history")
public class MemberRequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_req_his_seq", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_seq")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_info_isbn")
    private BookInfo bookInfo;

    @Column(name = "request_reason", length = 200, nullable = false)
    private String requestReason;

    @CreatedDate
    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @ColumnDefault("false")
    @Column(name = "is_approved", nullable = false)
    private boolean isApproved;

    @Builder
    public MemberRequestHistory(Member member, BookInfo bookInfo, String requestReason) {
        this.member = member;
        this.bookInfo = bookInfo;
        this.requestReason = requestReason;
    }

    public void doApprove() {
        this.isApproved = true;
    }
}
