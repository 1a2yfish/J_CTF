package com.CTF.j_ctf.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardDTO {
    // 实体ID（用户ID/团队ID）
    private Integer entityId;
    // 实体类型（USER/TEAM）
    private String entityType;
    // 实体名称（用户名/团队名）
    private String entityName;
    // 竞赛ID
    private Integer competitionId;
    // 总分数
    private Integer totalScore;
    // 解题数
    private Integer solveCount;
}