package com.CTF.j_ctf.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Flag提交记录表实体
 * 整合原两个FlagSubmission实体，兼容个人/战队提交、关联挑战/Flag的业务场景
 */
@Setter
@Getter
@Entity
@Table(name = "FlagSubmission")
public class FlagSubmission {
    /**
     * 提交记录主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SubmissionID")
    private Integer submissionID;

    /**
     * 提交用户（必选）
     */
    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private User user;

    /**
     * 关联的挑战（可选，部分场景可能仅关联Flag）
     */
    @ManyToOne
    @JoinColumn(name = "ChallengeID")
    private Challenge challenge;

    /**
     * 关联的Flag（可选，部分场景可能仅关联Challenge）
     */
    @ManyToOne
    @JoinColumn(name = "FlagID")
    private Flag flag;

    /**
     * 提交的战队（可选，战队提交时关联）
     */
    @ManyToOne
    @JoinColumn(name = "TeamID")
    private Team team;

    /**
     * 提交的内容（统一替换原submittedFlag/submittedValue，必选）
     */
    @Column(name = "SubmittedContent", nullable = false, length = 100)
    private String submittedContent;

    /**
     * 提交是否正确
     */
    @Column(name = "IsCorrect")
    private Boolean isCorrect;

    /**
     * 提交时间（默认自动填充当前时间）
     */
    @Column(name = "SubmitTime")
    private LocalDateTime submitTime;

    /**
     * 提交IP地址
     */
    @Column(name = "IPAddress", length = 45)
    private String ipAddress;

    /**
     * 提交的用户代理（浏览器/客户端信息）
     */
    @Column(name = "UserAgent", length = 500)
    private String userAgent;

    /**
     * 提交正确后获得的分数
     */
    @Column(name = "PointsAwarded")
    private Integer pointsAwarded;

    /**
     * 无参构造器：默认初始化提交时间为当前时间
     */
    public FlagSubmission() {
        this.submitTime = LocalDateTime.now();
    }

    /**
     * 构造器1：仅关联用户+挑战的提交（适配原第一个实体的业务场景）
     * @param user 提交用户
     * @param challenge 关联挑战
     * @param submittedContent 提交的Flag内容
     */
    public FlagSubmission(User user, Challenge challenge, String submittedContent) {
        this();
        this.user = user;
        this.challenge = challenge;
        this.submittedContent = submittedContent;
    }

    /**
     * 构造器2：仅关联用户+Flag的提交（适配原第二个实体的基础业务场景）
     * @param user 提交用户
     * @param flag 关联Flag
     * @param submittedContent 提交的Flag内容
     */
    public FlagSubmission(User user, Flag flag, String submittedContent) {
        this();
        this.user = user;
        this.flag = flag;
        this.submittedContent = submittedContent;
    }

    /**
     * 构造器3：关联用户+Flag+战队的提交（适配战队提交的扩展场景）
     * @param user 提交用户
     * @param flag 关联Flag
     * @param team 关联战队
     * @param submittedContent 提交的Flag内容
     */
    public FlagSubmission(User user, Flag flag, Team team, String submittedContent) {
        this();
        this.user = user;
        this.flag = flag;
        this.team = team;
        this.submittedContent = submittedContent;
    }

    /**
     * 构造器4：全关联提交（用户+挑战+Flag+战队，适配复杂业务场景）
     * @param user 提交用户
     * @param challenge 关联挑战
     * @param flag 关联Flag
     * @param team 关联战队
     * @param submittedContent 提交的Flag内容
     */
    public FlagSubmission(User user, Challenge challenge, Flag flag, Team team, String submittedContent) {
        this();
        this.user = user;
        this.challenge = challenge;
        this.flag = flag;
        this.team = team;
        this.submittedContent = submittedContent;
    }
}