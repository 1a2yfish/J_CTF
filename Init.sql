-- =============================================
-- CTF竞赛系统数据库初始化脚本
-- 数据库：competition_system
-- 适配：MySQL 8.0+（支持窗口函数、检查约束等）
-- =============================================

-- 1. 创建数据库并指定字符集（避免中文乱码）
CREATE DATABASE IF NOT EXISTS competition_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE competition_system;

-- 2. 用户基表（User）：所有用户的公共主键表
-- 注：采用“单表继承”的变种设计，Administrator/OrdinaryUser通过外键关联此表
CREATE TABLE IF NOT EXISTS `User` (
                                      `userID` INT NOT NULL AUTO_INCREMENT COMMENT '用户唯一主键ID',
                                      `userPassword` VARCHAR(100) NOT NULL COMMENT '用户密码（建议存储加密后的哈希值）',
    `CreateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '用户创建时间',
    PRIMARY KEY (`userID`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基表（所有用户的公共主键）';

-- 3. 管理员表（Administrator）：继承User表
CREATE TABLE IF NOT EXISTS `Administrator` (
                                               `userID` INT NOT NULL COMMENT '管理员ID，关联User表主键',
                                               `AdminName` VARCHAR(50) NOT NULL COMMENT '管理员姓名',
    `AdminRole` VARCHAR(20) DEFAULT 'SYSTEM' COMMENT '管理员角色（SYSTEM/OPERATOR/AUDITOR）',
    PRIMARY KEY (`userID`),
    CONSTRAINT `fk_admin_user` FOREIGN KEY (`userID`) REFERENCES `User` (`userID`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表（继承User）';

-- 4. 普通用户表（OrdinaryUser）：继承User表
CREATE TABLE IF NOT EXISTS `OrdinaryUser` (
                                              `userID` INT NOT NULL COMMENT '普通用户ID，关联User表主键',
                                              `PhoneNumber` VARCHAR(11) UNIQUE COMMENT '手机号（唯一，11位）',
    `userEmail` VARCHAR(100) UNIQUE COMMENT '用户邮箱（唯一）',
    `userName` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名（唯一）',
    `Gender` CHAR(1) COMMENT '性别（男/女/未知，用M/F/U表示）',
    `School_Workunit` VARCHAR(100) COMMENT '学校/工作单位',
    `RegisterTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `UserStatus` TINYINT(1) DEFAULT 1 COMMENT '用户状态（1-正常 0-禁用）',
    PRIMARY KEY (`userID`),
    CONSTRAINT `fk_ordinary_user` FOREIGN KEY (`userID`) REFERENCES `User` (`userID`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='普通用户表（继承User）';

-- 5. 竞赛主表（Competition）：CTF竞赛的核心配置表
CREATE TABLE IF NOT EXISTS `Competition` (
                                             `CompetitionID` INT NOT NULL AUTO_INCREMENT COMMENT '竞赛ID，主键自增',
                                             `userID` INT NOT NULL COMMENT '竞赛创建者ID，关联User表',
                                             `Title` VARCHAR(100) NOT NULL COMMENT '竞赛标题',
    `Introduction` TEXT NOT NULL COMMENT '竞赛介绍',
    `TeamSizeLimit` INT NOT NULL COMMENT '每支队伍的人数限制',
    `StartTime` DATETIME NOT NULL COMMENT '竞赛开始时间',
    `EndTime` DATETIME NOT NULL COMMENT '竞赛结束时间',
    `PublishTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '竞赛发布时间',
    `AuditTime` DATETIME DEFAULT NULL COMMENT '竞赛审核时间',
    `Status` VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '竞赛状态（DRAFT/PUBLISHED/ONGOING/FINISHED/CANCELLED）',
    `AuditStatus` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审核状态（PENDING/APPROVED/REJECTED）',
    `AuditRemark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    `MaxTeams` INT DEFAULT NULL COMMENT '最大参赛队伍数',
    `IsPublic` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否公开竞赛，默认公开',
    PRIMARY KEY (`CompetitionID`),
    CONSTRAINT `fk_competition_creator` FOREIGN KEY (`userID`) REFERENCES `User` (`userID`) ON DELETE RESTRICT,
    INDEX `idx_competition_creator` (`userID`),
    INDEX `idx_competition_status` (`Status`),
    INDEX `idx_competition_audit_status` (`AuditStatus`),
    -- 检查约束：结束时间必须晚于开始时间
    CHECK (`EndTime` > `StartTime`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CTF竞赛主表';

-- 6. 团队表（Team）：参赛战队表
CREATE TABLE `Team` (
                        `TeamID` INT NOT NULL AUTO_INCREMENT COMMENT '团队ID，主键',
                        `CompetitionID` INT NOT NULL COMMENT '赛事ID，关联Competition表主键',
                        `TeamName` VARCHAR(100) NOT NULL COMMENT '团队名称',
                        `CreationTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '团队创建时间，默认当前时间',
                        `AuditState` CHAR(1) NOT NULL DEFAULT '0' COMMENT '审核状态：0-待审核，1-通过，2-拒绝',
                        `Description` VARCHAR(500) DEFAULT NULL COMMENT '团队描述',
                        `AuditRemark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
                        `AuditTime` DATETIME DEFAULT NULL COMMENT '审核时间',
                        `CaptainID` INT DEFAULT NULL COMMENT '队长ID，关联User表主键',
                        PRIMARY KEY (`TeamID`),
    -- 联合唯一约束：同一赛事下团队名称不能重复
                        UNIQUE KEY `uk_competition_teamname` (`CompetitionID`, `TeamName`),
    -- 外键约束：关联赛事表
                        CONSTRAINT `fk_team_competition` FOREIGN KEY (`CompetitionID`) REFERENCES `Competition` (`CompetitionID`) ON DELETE RESTRICT ON UPDATE CASCADE,
    -- 外键约束：关联用户表（队长）
                        CONSTRAINT `fk_team_captain` FOREIGN KEY (`CaptainID`) REFERENCES `User` (`userID`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '团队表';

CREATE TABLE `TeamMember` (
                              `TeamID` INT NOT NULL COMMENT '团队ID，关联Team表主键',
                              `userID` INT NOT NULL COMMENT '用户ID，关联User表主键',
                              PRIMARY KEY (`TeamID`, `userID`), -- 联合主键，避免重复关联
    -- 外键约束：关联团队表
                              CONSTRAINT `fk_teammember_team` FOREIGN KEY (`TeamID`) REFERENCES `Team` (`TeamID`) ON DELETE CASCADE ON UPDATE CASCADE,
    -- 外键约束：关联用户表
                              CONSTRAINT `fk_teammember_user` FOREIGN KEY (`userID`) REFERENCES `User` (`userID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '团队-成员关联表（多对多）';

CREATE TABLE `TeamApplication` (
                                   `ApplicationID` INT NOT NULL AUTO_INCREMENT COMMENT '申请ID，主键',
                                   `TeamID` INT NOT NULL COMMENT '团队ID，关联Team表主键',
                                   `ApplicantID` INT NOT NULL COMMENT '申请人ID，关联User表主键',
                                   `Status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态：PENDING-待处理，APPROVED-已通过，REJECTED-已拒绝',
                                   `ApplyTime` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间，默认当前时间',
                                   `ProcessTime` DATETIME DEFAULT NULL COMMENT '处理时间（审核/审批时间）',
                                   `Remark` VARCHAR(500) DEFAULT NULL COMMENT '申请/处理备注',
                                   PRIMARY KEY (`ApplicationID`),
    -- 联合索引：优化“按团队+申请人”的查询（避免同一用户重复向同一团队申请）
                                   UNIQUE KEY `uk_team_applicant` (`TeamID`, `ApplicantID`),
    -- 外键约束：关联团队表
                                   CONSTRAINT `fk_application_team` FOREIGN KEY (`TeamID`) REFERENCES `Team` (`TeamID`) ON DELETE CASCADE ON UPDATE CASCADE,
    -- 外键约束：关联用户表（申请人）
                                   CONSTRAINT `fk_application_applicant` FOREIGN KEY (`ApplicantID`) REFERENCES `User` (`userID`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '团队加入申请表';

-- 7. 题目表（Challenge）：CTF竞赛题目表
CREATE TABLE IF NOT EXISTS `Challenge` (
                                           `ChallengeID` INT NOT NULL AUTO_INCREMENT COMMENT '题目ID，主键自增',
                                           `CompetitionID` INT NOT NULL COMMENT '关联的竞赛ID，外键',
                                           `Title` VARCHAR(100) NOT NULL COMMENT '题目名称',
    `Description` TEXT NOT NULL COMMENT '题目描述',
    `Category` VARCHAR(50) NOT NULL COMMENT '题目分类（Web/Pwn/Crypto/Reverse/Misc）',
    `Difficulty` VARCHAR(20) NOT NULL COMMENT '难度（Easy/Medium/Hard/Insane）',
    `Points` INT NOT NULL COMMENT '题目分值',
    `Flag` VARCHAR(100) NOT NULL COMMENT '题目默认FLAG',
    `IsActive` TINYINT(1) DEFAULT 1 COMMENT '是否启用，默认启用',
    `CreateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `UpdateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动更新）',
    `AttachmentUrl` VARCHAR(500) DEFAULT NULL COMMENT '附件下载链接',
    `Hint` VARCHAR(500) DEFAULT NULL COMMENT '题目简易提示',
    `SolveCount` INT DEFAULT 0 COMMENT '解题人数，默认0',
    PRIMARY KEY (`ChallengeID`),
    CONSTRAINT `fk_challenge_competition` FOREIGN KEY (`CompetitionID`) REFERENCES `Competition` (`CompetitionID`) ON DELETE CASCADE,
    INDEX `idx_competition_id` (`CompetitionID`),
    INDEX `idx_challenge_category` (`Category`),
    INDEX `idx_challenge_difficulty` (`Difficulty`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CTF竞赛题目表';

-- 8. 题目提示表（ChallengeHint）：题目详细提示表
CREATE TABLE IF NOT EXISTS `ChallengeHint` (
                                               `HintID` INT NOT NULL AUTO_INCREMENT COMMENT '提示ID，主键自增',
                                               `ChallengeID` INT NOT NULL COMMENT '关联的题目ID，外键',
                                               `Content` TEXT NOT NULL COMMENT '提示内容',
                                               `Cost` INT DEFAULT NULL COMMENT '查看提示消耗的分数（可为空）',
                                               `CreateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提示创建时间',
                                               PRIMARY KEY (`HintID`),
    CONSTRAINT `fk_hint_challenge` FOREIGN KEY (`ChallengeID`) REFERENCES `Challenge` (`ChallengeID`) ON DELETE CASCADE,
    INDEX `idx_challenge_id` (`ChallengeID`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CTF竞赛题目提示表';

-- 9. Flag管理表（Flag）：动态/静态Flag配置表
CREATE TABLE IF NOT EXISTS `Flag` (
                                      `flagID` INT NOT NULL AUTO_INCREMENT COMMENT 'FlagID，主键自增',
                                      `userID` INT NOT NULL COMMENT '关联的用户ID，外键',
                                      `CompetitionID` INT NOT NULL COMMENT '关联的竞赛ID，外键',
                                      `ChallengeID` INT DEFAULT NULL COMMENT '关联的题目ID，外键（可为空）',
                                      `TeamID` INT DEFAULT NULL COMMENT '关联的战队ID，外键（可为空）',
                                      `Value` VARCHAR(100) NOT NULL COMMENT 'Flag值',
    `Status` TINYINT NOT NULL DEFAULT 0 COMMENT 'Flag状态：0-未使用 1-已使用 2-已过期',
    `CreateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Flag创建时间',
    `UseTime` DATETIME DEFAULT NULL COMMENT 'Flag使用时间',
    `ExpireTime` DATETIME DEFAULT NULL COMMENT 'Flag过期时间',
    `Points` INT DEFAULT NULL COMMENT 'Flag对应的分值',
    `Type` VARCHAR(20) DEFAULT NULL COMMENT 'Flag类型（DYNAMIC/STATIC/BONUS）',
    `Description` VARCHAR(500) DEFAULT NULL COMMENT 'Flag描述',
    PRIMARY KEY (`flagID`),
    CONSTRAINT `fk_flag_user` FOREIGN KEY (`userID`) REFERENCES `User` (`userID`) ON DELETE RESTRICT,
    CONSTRAINT `fk_flag_competition` FOREIGN KEY (`CompetitionID`) REFERENCES `Competition` (`CompetitionID`) ON DELETE CASCADE,
    CONSTRAINT `fk_flag_challenge` FOREIGN KEY (`ChallengeID`) REFERENCES `Challenge` (`ChallengeID`) ON DELETE SET NULL,
    CONSTRAINT `fk_flag_team` FOREIGN KEY (`TeamID`) REFERENCES `Team` (`TeamID`) ON DELETE SET NULL,
    INDEX `idx_flag_user` (`userID`),
    INDEX `idx_flag_competition` (`CompetitionID`),
    INDEX `idx_flag_challenge` (`ChallengeID`),
    INDEX `idx_flag_team` (`TeamID`),
    INDEX `idx_flag_status` (`Status`),
    INDEX `idx_flag_type` (`Type`),
    -- 检查约束：Flag状态只能是0/1/2
    CHECK (`Status` IN (0, 1, 2))
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CTF竞赛Flag管理表';

-- 10. Flag提交记录表（FlagSubmission）：用户/战队提交Flag的记录
CREATE TABLE IF NOT EXISTS `FlagSubmission` (
                                                `SubmissionID` INT NOT NULL AUTO_INCREMENT COMMENT '提交记录ID，主键自增',
                                                `userID` INT NOT NULL COMMENT '提交用户ID，外键（必选）',
                                                `CompetitionID` INT NOT NULL COMMENT '关联的竞赛ID，外键（必选）', -- 新增核心字段
                                                `ChallengeID` INT DEFAULT NULL COMMENT '关联的题目ID，外键（可选）',
                                                `FlagID` INT DEFAULT NULL COMMENT '关联的FlagID，外键（可选）',
                                                `TeamID` INT DEFAULT NULL COMMENT '关联的战队ID，外键（可选）',
                                                `SubmittedContent` VARCHAR(100) NOT NULL COMMENT '提交的Flag内容',
    `IsCorrect` TINYINT(1) DEFAULT NULL COMMENT '提交是否正确：1-正确 0-错误',
    `SubmitTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `IPAddress` VARCHAR(45) DEFAULT NULL COMMENT '提交者IP地址（适配IPv4/IPv6）',
    `UserAgent` VARCHAR(500) DEFAULT NULL COMMENT '提交者客户端代理信息',
    `PointsAwarded` INT DEFAULT NULL COMMENT '提交正确后获得的分数',
    PRIMARY KEY (`SubmissionID`),
    CONSTRAINT `fk_submission_user` FOREIGN KEY (`userID`) REFERENCES `User` (`userID`) ON DELETE RESTRICT,
    CONSTRAINT `fk_submission_competition` FOREIGN KEY (`CompetitionID`) REFERENCES `Competition` (`CompetitionID`) ON DELETE CASCADE, -- 新增外键
    CONSTRAINT `fk_submission_challenge` FOREIGN KEY (`ChallengeID`) REFERENCES `Challenge` (`ChallengeID`) ON DELETE SET NULL,
    CONSTRAINT `fk_submission_flag` FOREIGN KEY (`FlagID`) REFERENCES `Flag` (`flagID`) ON DELETE SET NULL,
    CONSTRAINT `fk_submission_team` FOREIGN KEY (`TeamID`) REFERENCES `Team` (`TeamID`) ON DELETE SET NULL,
    INDEX `idx_submission_user` (`userID`),
    INDEX `idx_submission_competition` (`CompetitionID`), -- 新增索引
    INDEX `idx_submission_challenge` (`ChallengeID`),
    INDEX `idx_submission_flag` (`FlagID`),
    INDEX `idx_submission_team` (`TeamID`),
    INDEX `idx_submission_correct` (`IsCorrect`),
    INDEX `idx_submission_time` (`SubmitTime`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CTF竞赛Flag提交记录表';

-- 11. 分数变动记录表（Score）：用户/战队的分数增减记录
CREATE TABLE IF NOT EXISTS `Score` (
                                       `ChangeID` INT NOT NULL AUTO_INCREMENT COMMENT '分数变动记录ID，主键自增',
                                       `userID` INT NOT NULL COMMENT '关联的用户ID，外键（必选）',
                                       `CompetitionID` INT NOT NULL COMMENT '关联的竞赛ID，外键（必选）',
                                       `TeamID` INT DEFAULT NULL COMMENT '关联的战队ID，外键（可选）',
                                       `ChallengeID` INT DEFAULT NULL COMMENT '关联的题目ID，外键（可选）',
                                       `FlagID` INT DEFAULT NULL COMMENT '关联的FlagID，外键（可选）',
                                       `Score` INT NOT NULL COMMENT '分数值（可正可负）',
                                       `CreateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分数变动时间',
                                       `Type` VARCHAR(20) DEFAULT NULL COMMENT '分数变动类型（FLAG_SUBMISSION/BONUS/PENALTY/ADJUSTMENT）',
    `Description` VARCHAR(500) DEFAULT NULL COMMENT '分数变动描述',
    `IsValid` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '分数是否有效（1-有效 0-无效）',
    PRIMARY KEY (`ChangeID`),
    CONSTRAINT `fk_score_user` FOREIGN KEY (`userID`) REFERENCES `User` (`userID`) ON DELETE RESTRICT,
    CONSTRAINT `fk_score_competition` FOREIGN KEY (`CompetitionID`) REFERENCES `Competition` (`CompetitionID`) ON DELETE CASCADE,
    CONSTRAINT `fk_score_team` FOREIGN KEY (`TeamID`) REFERENCES `Team` (`TeamID`) ON DELETE SET NULL,
    CONSTRAINT `fk_score_challenge` FOREIGN KEY (`ChallengeID`) REFERENCES `Challenge` (`ChallengeID`) ON DELETE SET NULL,
    CONSTRAINT `fk_score_flag` FOREIGN KEY (`FlagID`) REFERENCES `Flag` (`flagID`) ON DELETE SET NULL,
    INDEX `idx_score_user` (`userID`),
    INDEX `idx_score_competition` (`CompetitionID`),
    INDEX `idx_score_team` (`TeamID`),
    INDEX `idx_score_challenge` (`ChallengeID`),
    INDEX `idx_score_flag` (`FlagID`),
    INDEX `idx_score_type` (`Type`),
    INDEX `idx_score_valid` (`IsValid`),
    INDEX `idx_score_create_time` (`CreateTime`),
    -- 联合索引：提升“用户+竞赛”的分数聚合效率
    INDEX `idx_user_competition` (`userID`, `CompetitionID`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CTF竞赛分数变动记录表';

-- 12. 解题报告表（WriteUp）：用户提交的解题报告
CREATE TABLE IF NOT EXISTS `WriteUp` (
                                         `WriteUpID` INT NOT NULL AUTO_INCREMENT COMMENT '报告ID，主键自增',
                                         `userID` INT NOT NULL COMMENT '用户ID，外键',
                                         `CompetitionID` INT NOT NULL COMMENT '竞赛ID，外键',
                                         `ChallengeID` INT DEFAULT NULL COMMENT '关联题目ID，外键',
                                         `Title` VARCHAR(100) NOT NULL COMMENT '报告标题',
    `Content` TEXT NOT NULL COMMENT '报告内容',
    `CreateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `UpdateTime` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`WriteUpID`),
    CONSTRAINT `fk_writeup_user` FOREIGN KEY (`userID`) REFERENCES `User` (`userID`) ON DELETE CASCADE,
    CONSTRAINT `fk_writeup_competition` FOREIGN KEY (`CompetitionID`) REFERENCES `Competition` (`CompetitionID`) ON DELETE CASCADE,
    CONSTRAINT `fk_writeup_challenge` FOREIGN KEY (`ChallengeID`) REFERENCES `Challenge` (`ChallengeID`) ON DELETE SET NULL,
    INDEX `idx_writeup_user` (`userID`),
    INDEX `idx_writeup_competition` (`CompetitionID`),
    INDEX `idx_writeup_challenge` (`ChallengeID`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CTF竞赛解题报告表';

-- 13. 分数汇总视图（ScoreSummary）：实时统计用户/战队的分数、排名、解题数
-- 注：视图为虚拟表，仅支持查询，不支持增删改
CREATE OR REPLACE VIEW `ScoreSummary` AS
WITH UserScoreStats AS (
    -- 统计用户个人分数和解题数据
    SELECT
        u.userID AS entityID,
        'USER' AS entityType,
        ou.userName AS entityName,
        s.CompetitionID,
        COALESCE(SUM(CASE WHEN s.IsValid = 1 THEN s.Score ELSE 0 END), 0) AS totalScore,
        COALESCE(COUNT(DISTINCT fs.ChallengeID), 0) AS solveCount,
        MAX(CASE WHEN fs.IsCorrect = 1 THEN fs.SubmitTime ELSE NULL END) AS lastSubmitTime
    FROM `User` u
             LEFT JOIN `OrdinaryUser` ou ON u.userID = ou.userID
             LEFT JOIN `Score` s ON u.userID = s.userID
             LEFT JOIN `FlagSubmission` fs ON u.userID = fs.userID AND s.CompetitionID = fs.CompetitionID
    GROUP BY u.userID, ou.userName, s.CompetitionID
),
     TeamScoreStats AS (
         -- 统计战队分数和解题数据
         SELECT
             t.TeamID AS entityID,
             'TEAM' AS entityType,
             t.TeamName AS entityName,
             s.CompetitionID,
             COALESCE(SUM(CASE WHEN s.IsValid = 1 THEN s.Score ELSE 0 END), 0) AS totalScore,
             COALESCE(COUNT(DISTINCT fs.ChallengeID), 0) AS solveCount,
             MAX(CASE WHEN fs.IsCorrect = 1 THEN fs.SubmitTime ELSE NULL END) AS lastSubmitTime
         FROM `Team` t
                  LEFT JOIN `Score` s ON t.TeamID = s.TeamID
                  LEFT JOIN `FlagSubmission` fs ON t.TeamID = fs.TeamID AND s.CompetitionID = fs.CompetitionID
         GROUP BY t.TeamID, t.TeamName, s.CompetitionID
     ),
     CombinedStats AS (
         -- 合并用户和战队数据，生成唯一ID
         SELECT
             CONCAT(entityType, '_', entityID) AS id,
             entityID,
             entityType,
             entityName,
             CompetitionID,
             totalScore,
             solveCount,
             lastSubmitTime
         FROM UserScoreStats
         UNION ALL
         SELECT
             CONCAT(entityType, '_', entityID) AS id,
             entityID,
             entityType,
             entityName,
             CompetitionID,
             totalScore,
             solveCount,
             lastSubmitTime
         FROM TeamScoreStats
     )
-- 计算排名（按竞赛分组，总分降序、最后提交时间升序）
SELECT
    cs.id,
    cs.entityID,
    cs.entityType,
    cs.entityName,
    cs.CompetitionID,
    cs.totalScore,
    cs.solveCount,
    cs.lastSubmitTime,
    ROW_NUMBER() OVER (
        PARTITION BY cs.CompetitionID
        ORDER BY cs.totalScore DESC, cs.lastSubmitTime ASC
        ) AS `Rank`
FROM CombinedStats cs
WHERE cs.CompetitionID IS NOT NULL;

-- 脚本执行完成提示
SELECT 'CTF竞赛系统数据库初始化完成！' AS `Result`;