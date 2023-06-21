package com.plee.library.domain.member;

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
    @JoinColumn(name = "member_seq")
    private Member member;

    @Column(length = 13, nullable = false)
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String description;

    @CreatedDate
    @Column(name = "req_date")
    private LocalDateTime reqDate;

    @ColumnDefault("false")
    @Column(name = "is_approved")
    private boolean isApproved;

    @Builder
    public MemberRequestHistory(Member member, String isbn, String title, String author, String description) {
        this.member = member;
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.description = description;
    }

    public void approve() {
        this.isApproved = true;
    }
}
