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

import java.time.LocalDateTime;

@Getter
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
    @JoinColumn(name = "member_seq", nullable = false)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_info_seq", nullable = false)
    private BookInfo bookInfo;

    @Column(name = "req_reason", length = 200, nullable = false)
    private String reqReason;

    @CreatedDate
    @Column(name = "req_date")
    private LocalDateTime reqDate;

    @ColumnDefault("false")
    @Column(name = "is_approved", nullable = false)
    private boolean isApproved;

    @Builder
    public MemberRequestHistory(Member member, BookInfo bookInfo, String reqReason) {
        this.member = member;
        this.bookInfo = bookInfo;
        this.reqReason = reqReason;
    }

    public void doApprove() {
        this.isApproved = true;
    }
}
