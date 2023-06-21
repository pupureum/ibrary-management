package com.plee.library.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DynamicInsert
@Table(name = "member", uniqueConstraints = {@UniqueConstraint(name = "login_id_emp_no_unique", columnNames = {"login_id", "emp_no"})})
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_seq", updatable = false)
    private Long id;

    @Column(length = 25, nullable = false)
    private String name;

    @Column(length = 50, nullable = false, unique = true)
    private String loginId;

    @Column(length = 15, nullable = false)
    private String password;

    @Column(length = 7, nullable = false)
    private String empNo;

    @ColumnDefault("'ROLE_USER'")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberLoanHistory> memberLoanHistories = new ArrayList<MemberLoanHistory>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberRequestHistory> memberRequestHistories = new ArrayList<MemberRequestHistory>();


    @Column(name = "penalty_end_date")
    private LocalDateTime penaltyEndDate;

    @Builder
    public Member(String name, String loginId, String password, String empNo) {
        this.name = name;
        this.loginId = loginId;
        this.password = password;
        this.empNo = empNo;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
