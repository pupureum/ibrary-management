package com.plee.library.domain.member;

import com.plee.library.domain.BaseTimeEntity;
import com.plee.library.domain.book.BookInfo;
import com.plee.library.domain.book.Book;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DynamicInsert
@Table(name = "member", uniqueConstraints = {@UniqueConstraint(name = "login_id_unique", columnNames = {"login_id"})})
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_seq", updatable = false)
    private Long id;

    @Column(name = "name", length = 25, nullable = false)
    private String name;

    @Column(name = "login_id", length = 40, nullable = false)
    private String loginId;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'MEMBER'")
    @Column(name = "role", nullable = false)
    private Role role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberLoanHistory> memberLoanHistories = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberRequestHistory> memberRequestHistories = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<MemberBookmark> memberBookmarks = new HashSet<>();

    @Builder
    public Member(Long id, String name, String loginId, String password, Role role) {
        this.id = id;
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public void addBookRequest(BookInfo bookInfo, String reqReason) {
        this.memberRequestHistories.add(new MemberRequestHistory(this, bookInfo, reqReason));
    }

    public void addBookmark(Book book) {
        this.memberBookmarks.add(new MemberBookmark(this, book));
    }

    public void loanBook(Book book) {
        this.memberLoanHistories.add(new MemberLoanHistory(this, book.getBookInfo()));
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    public void changeName(String name) {
        this.name = name;
    }
}
