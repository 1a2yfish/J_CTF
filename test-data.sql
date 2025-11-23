-- =============================================
-- CTF竞赛平台测试数据脚本（修复版）
-- 数据库：competition_system
-- 说明：插入测试数据，确保平台可以正常运行
-- 更新：使用修复后的数据库结构，包含ChallengeHint.UpdateTime和Challenge.CreatorID
-- =============================================

USE competition_system;

-- 清空现有数据（可选，谨慎使用）
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE `WriteUp`;
-- TRUNCATE TABLE `Score`;
-- TRUNCATE TABLE `Flag`;
-- TRUNCATE TABLE `FlagSubmission`;
-- TRUNCATE TABLE `ChallengeHint`;
-- TRUNCATE TABLE `Challenge`;
-- TRUNCATE TABLE `TeamApplication`;
-- TRUNCATE TABLE `TeamMember`;
-- TRUNCATE TABLE `Team`;
-- TRUNCATE TABLE `Competition`;
-- TRUNCATE TABLE `User`;
-- SET FOREIGN_KEY_CHECKS = 1;

-- 1. 插入管理员用户
-- 密码：aaa1，BCrypt加密值：$2a$10$pUFdG0zRBt4Ic2SfUM43CuRi/N2PNxQgHwomX9d6zxfDSGbzRiEbm
INSERT INTO `User` (`userID`, `userPassword`, `CreateTime`, `UserType`, `UserName`, `AdminRole`, `UserStatus`)
VALUES 
(1, '$2a$10$pUFdG0zRBt4Ic2SfUM43CuRi/N2PNxQgHwomX9d6zxfDSGbzRiEbm', NOW(), 'ADMIN', 'admin', 'SYSTEM', 1)
ON DUPLICATE KEY UPDATE 
    `userPassword` = VALUES(`userPassword`),
    `UserType` = VALUES(`UserType`),
    `AdminRole` = VALUES(`AdminRole`);

-- 2. 插入普通用户（密码都是：123456）
-- BCrypt加密值：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
INSERT INTO `User` (`userID`, `userPassword`, `CreateTime`, `UserType`, `UserName`, `PhoneNumber`, `userEmail`, `Gender`, `School_Workunit`, `RegisterTime`, `UserStatus`)
VALUES 
(2, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW(), 'ORDINARY', 'testuser1', '13800138001', 'testuser1@example.com', 'M', '测试大学', NOW(), 1),
(3, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW(), 'ORDINARY', 'testuser2', '13800138002', 'testuser2@example.com', 'F', '测试大学', NOW(), 1),
(4, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW(), 'ORDINARY', 'testuser3', '13800138003', 'testuser3@example.com', 'M', '测试大学', NOW(), 1),
(5, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW(), 'ORDINARY', 'testuser4', '13800138004', 'testuser4@example.com', 'F', '测试大学', NOW(), 1),
(6, '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NOW(), 'ORDINARY', 'testuser5', '13800138005', 'testuser5@example.com', 'M', '测试大学', NOW(), 1)
ON DUPLICATE KEY UPDATE 
    `userPassword` = VALUES(`userPassword`),
    `UserType` = VALUES(`UserType`);

-- 3. 插入竞赛数据
INSERT INTO `Competition` (`CompetitionID`, `userID`, `Title`, `Introduction`, `TeamSizeLimit`, `StartTime`, `EndTime`, `PublishTime`, `Status`, `AuditStatus`, `IsPublic`)
VALUES 
(1, 1, '2024春季CTF挑战赛', '这是一场面向高校学生的CTF竞赛，包含Web、Pwn、Crypto、Reverse、Misc等多个方向的题目。', 5, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY), NOW(), 'PUBLISHED', 'APPROVED', 1),
(2, 1, '网络安全实战演练', '企业级网络安全实战演练，模拟真实攻击场景，提升安全防护能力。', 3, DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), NOW(), 'PUBLISHED', 'APPROVED', 1),
(3, 1, 'CTF新手训练营', '适合CTF新手的入门级竞赛，题目难度较低，帮助新手快速入门。', 4, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY), 'FINISHED', 'APPROVED', 1)
ON DUPLICATE KEY UPDATE 
    `Title` = VALUES(`Title`),
    `Status` = VALUES(`Status`);

-- 4. 插入题目数据（包含CreatorID字段）
INSERT INTO `Challenge` (`ChallengeID`, `CompetitionID`, `Title`, `Description`, `Category`, `Difficulty`, `Points`, `Flag`, `IsActive`, `CreateTime`, `UpdateTime`, `SolveCount`, `CreatorID`)
VALUES 
-- 竞赛1的题目
(1, 1, 'Web安全入门', '这是一个简单的Web安全题目，尝试找到隐藏的Flag。提示：查看页面源代码。', 'Web', 'Easy', 100, 'CTF{web_security_001}', 1, NOW(), NOW(), 0, 1),
(2, 1, 'SQL注入基础', '尝试通过SQL注入获取管理员密码。数据库表名：users，字段：username, password。', 'Web', 'Medium', 200, 'CTF{sql_injection_001}', 1, NOW(), NOW(), 0, 1),
(3, 1, '密码学挑战', '这是一个简单的凯撒密码题目。密文：KHOOR ZRUOG，请解密。', 'Crypto', 'Easy', 150, 'CTF{crypto_caesar_001}', 1, NOW(), NOW(), 0, 1),
(4, 1, '逆向工程', '这是一个简单的逆向题目，请分析程序逻辑并找到Flag。', 'Reverse', 'Medium', 250, 'CTF{reverse_001}', 1, NOW(), NOW(), 0, 1),
(5, 1, 'Pwn基础', '这是一个简单的栈溢出题目，尝试获取shell。', 'Pwn', 'Hard', 300, 'CTF{pwn_stack_001}', 1, NOW(), NOW(), 0, 1),
-- 竞赛2的题目
(6, 2, 'XSS攻击', '尝试在留言板中注入XSS代码，获取管理员Cookie。', 'Web', 'Medium', 200, 'CTF{xss_attack_001}', 1, NOW(), NOW(), 0, 1),
(7, 2, '文件上传漏洞', '尝试上传恶意文件并执行代码。', 'Web', 'Hard', 300, 'CTF{file_upload_001}', 1, NOW(), NOW(), 0, 1),
-- 竞赛3的题目
(8, 3, 'Hello CTF', '欢迎来到CTF世界！Flag就在题目描述中。', 'Misc', 'Easy', 50, 'CTF{hello_world}', 1, NOW(), NOW(), 0, 1),
(9, 3, 'Base64编码', '这是一个Base64编码的字符串：Q1RGe2Jhc2U2NF9lbmNvZGV9，请解码。', 'Misc', 'Easy', 50, 'CTF{base64_encode}', 1, NOW(), NOW(), 0, 1)
ON DUPLICATE KEY UPDATE 
    `Title` = VALUES(`Title`),
    `UpdateTime` = NOW();

-- 5. 插入题目提示（包含UpdateTime字段）
INSERT INTO `ChallengeHint` (`HintID`, `ChallengeID`, `Content`, `Cost`, `CreateTime`, `UpdateTime`)
VALUES 
(1, 1, '查看HTML源代码，特别是注释部分。', 10, NOW(), NOW()),
(2, 1, '如果找不到，可以尝试查看页面的JavaScript代码。', 10, NOW(), NOW()),
(3, 2, '尝试使用单引号闭合SQL语句。', 20, NOW(), NOW()),
(4, 2, '可以使用UNION SELECT来获取数据。', 20, NOW(), NOW()),
(5, 3, '凯撒密码的偏移量是3。', 15, NOW(), NOW()),
(6, 4, '使用IDA Pro或Ghidra进行静态分析。', 25, NOW(), NOW()),
(7, 4, '注意字符串常量和函数调用。', 25, NOW(), NOW()),
(8, 5, '注意栈的布局和返回地址的位置。', 30, NOW(), NOW()),
(9, 5, '可以使用ROP链来绕过NX保护。', 30, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    `Content` = VALUES(`Content`),
    `UpdateTime` = NOW();

-- 6. 插入团队数据
INSERT INTO `Team` (`TeamID`, `CompetitionID`, `TeamName`, `CreationTime`, `AuditState`, `Description`, `CaptainID`)
VALUES 
(1, 1, '零日漏洞', NOW(), '1', '专注于Web安全和漏洞挖掘的团队', 2),
(2, 1, '安全先锋队', NOW(), '1', 'CTF竞赛经验丰富的团队', 3),
(3, 1, '代码猎人', NOW(), '1', '擅长逆向工程和代码审计', 4),
(4, 2, '网络卫士', NOW(), '1', '企业安全防护专家团队', 2),
(5, 2, '极客联盟', NOW(), '0', '待审核的团队', 5)
ON DUPLICATE KEY UPDATE 
    `TeamName` = VALUES(`TeamName`),
    `AuditState` = VALUES(`AuditState`);

-- 7. 插入团队成员
INSERT INTO `TeamMember` (`TeamID`, `userID`, `JoinTime`)
VALUES 
-- 团队1成员（队长自动成为成员）
(1, 2, NOW()),
(1, 3, NOW()),
-- 团队2成员
(2, 3, NOW()),
(2, 4, NOW()),
(2, 5, NOW()),
-- 团队3成员
(3, 4, NOW()),
(3, 5, NOW()),
-- 团队4成员
(4, 2, NOW()),
(4, 6, NOW())
ON DUPLICATE KEY UPDATE 
    `JoinTime` = VALUES(`JoinTime`);

-- 8. 插入团队申请
INSERT INTO `TeamApplication` (`ApplicationID`, `TeamID`, `ApplicantID`, `Status`, `ApplyTime`, `ProcessTime`, `Remark`)
VALUES 
(1, 1, 6, 'PENDING', NOW(), NULL, '希望加入团队，有丰富的Web安全经验'),
(2, 2, 6, 'APPROVED', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), '已通过申请'),
(3, 3, 2, 'REJECTED', DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), '团队已满员')
ON DUPLICATE KEY UPDATE 
    `Status` = VALUES(`Status`),
    `ProcessTime` = VALUES(`ProcessTime`);

-- 9. 插入Flag提交记录（模拟一些已解决的题目）
INSERT INTO `FlagSubmission` (`SubmissionID`, `userID`, `CompetitionID`, `ChallengeID`, `TeamID`, `SubmittedContent`, `IsCorrect`, `SubmitTime`, `IPAddress`, `PointsAwarded`)
VALUES 
(1, 2, 1, 1, 1, 'CTF{web_security_001}', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), '192.168.1.100', 100),
(2, 3, 1, 3, 2, 'CTF{crypto_caesar_001}', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), '192.168.1.101', 150),
(3, 4, 1, 1, 3, 'CTF{wrong_flag}', 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE), '192.168.1.102', 0),
(4, 2, 1, 2, 1, 'CTF{sql_injection_001}', 1, DATE_SUB(NOW(), INTERVAL 15 MINUTE), '192.168.1.100', 200),
(5, 3, 1, 1, 2, 'CTF{web_security_001}', 1, DATE_SUB(NOW(), INTERVAL 10 MINUTE), '192.168.1.101', 100)
ON DUPLICATE KEY UPDATE 
    `IsCorrect` = VALUES(`IsCorrect`),
    `PointsAwarded` = VALUES(`PointsAwarded`);

-- 10. 插入分数记录
INSERT INTO `Score` (`ChangeID`, `userID`, `CompetitionID`, `TeamID`, `ChallengeID`, `Score`, `CreateTime`, `Type`, `Description`, `IsValid`)
VALUES 
(1, 2, 1, 1, 1, 100, DATE_SUB(NOW(), INTERVAL 2 HOUR), 'FLAG_SUBMISSION', '正确提交Flag获得分数', 1),
(2, 3, 1, 2, 3, 150, DATE_SUB(NOW(), INTERVAL 1 HOUR), 'FLAG_SUBMISSION', '正确提交Flag获得分数', 1),
(3, 2, 1, 1, 2, 200, DATE_SUB(NOW(), INTERVAL 15 MINUTE), 'FLAG_SUBMISSION', '正确提交Flag获得分数', 1),
(4, 3, 1, 2, 1, 100, DATE_SUB(NOW(), INTERVAL 10 MINUTE), 'FLAG_SUBMISSION', '正确提交Flag获得分数', 1)
ON DUPLICATE KEY UPDATE 
    `Score` = VALUES(`Score`),
    `CreateTime` = VALUES(`CreateTime`);

-- 11. 插入Flag管理记录
INSERT INTO `Flag` (`flagID`, `userID`, `CompetitionID`, `ChallengeID`, `TeamID`, `Value`, `Status`, `CreateTime`, `UseTime`, `Points`, `Type`)
VALUES 
(1, 2, 1, 1, 1, 'CTF{web_security_001}', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR), 100, 'STATIC'),
(2, 3, 1, 3, 2, 'CTF{crypto_caesar_001}', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR), 150, 'STATIC'),
(3, 2, 1, 2, 1, 'CTF{sql_injection_001}', 1, DATE_SUB(NOW(), INTERVAL 15 MINUTE), DATE_SUB(NOW(), INTERVAL 15 MINUTE), 200, 'STATIC')
ON DUPLICATE KEY UPDATE 
    `Status` = VALUES(`Status`),
    `UseTime` = VALUES(`UseTime`);

-- 12. 插入解题报告
INSERT INTO `WriteUp` (`WriteUpID`, `userID`, `CompetitionID`, `ChallengeID`, `Title`, `Content`, `CreateTime`, `UpdateTime`)
VALUES 
(1, 2, 1, 1, 'Web安全入门题解', '通过查看页面源代码，在HTML注释中找到了Flag。这是一个简单的Web安全入门题目，主要考察对HTML源码的检查能力。', DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR)),
(2, 3, 1, 3, '凯撒密码题解', '使用凯撒密码解密，偏移量为3，得到Flag。凯撒密码是一种简单的替换密码，通过将字母按照固定偏移量进行替换来实现加密。', DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE))
ON DUPLICATE KEY UPDATE 
    `Title` = VALUES(`Title`),
    `UpdateTime` = NOW();

-- 完成提示
SELECT '=============================================' AS `Separator`;
SELECT '测试数据插入完成！' AS `Result`;
SELECT '=============================================' AS `Separator`;
SELECT '管理员账号信息：' AS `Info`;
SELECT '  账号：admin' AS `Account`;
SELECT '  密码：aaa1' AS `Password`;
SELECT '=============================================' AS `Separator`;
SELECT '测试用户账号信息：' AS `Info`;
SELECT '  账号：testuser1 - testuser5' AS `Account`;
SELECT '  密码：123456' AS `Password`;
SELECT '=============================================' AS `Separator`;
SELECT '测试数据说明：' AS `Info`;
SELECT '  - 3个竞赛（1个进行中，1个即将开始，1个已结束）' AS `Detail1`;
SELECT '  - 9个题目（涵盖Web、Crypto、Reverse、Pwn、Misc）' AS `Detail2`;
SELECT '  - 5个团队（4个已审核通过，1个待审核）' AS `Detail3`;
SELECT '  - 部分题目已有提交记录和分数' AS `Detail4`;
SELECT '=============================================' AS `Separator`;
