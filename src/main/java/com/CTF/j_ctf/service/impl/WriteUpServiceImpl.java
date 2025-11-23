package com.CTF.j_ctf.service.impl;

import com.CTF.j_ctf.entity.Competition;
import com.CTF.j_ctf.entity.User;
import com.CTF.j_ctf.entity.WriteUp;
import com.CTF.j_ctf.repository.CompetitionRepository;
import com.CTF.j_ctf.repository.UserRepository;
import com.CTF.j_ctf.repository.WriteUpRepository;
import com.CTF.j_ctf.service.WriteUpService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WriteUpServiceImpl implements WriteUpService {
    private final WriteUpRepository writeUpRepository;
    private final UserRepository userRepository;
    private final CompetitionRepository competitionRepository;

    public WriteUpServiceImpl(WriteUpRepository writeUpRepository,
                              UserRepository userRepository,
                              CompetitionRepository competitionRepository) {
        this.writeUpRepository = writeUpRepository;
        this.userRepository = userRepository;
        this.competitionRepository = competitionRepository;
    }

    @Override
    public WriteUp createWriteUp(WriteUp writeUp) {
        // 验证数据
        validateWriteUp(writeUp);
        return writeUpRepository.save(writeUp);
    }

    @Override
    public WriteUp createWriteUp(Integer userId, Integer competitionId, String title, String content) {
        // 查找用户和竞赛
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Competition> competitionOpt = competitionRepository.findById(competitionId);

        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        if (competitionOpt.isEmpty()) {
            throw new IllegalArgumentException("竞赛不存在");
        }

        User user = userOpt.get();
        Competition competition = competitionOpt.get();

        // 检查是否已经提交过解题报告，如果存在则更新，否则创建
        List<WriteUp> existingWriteUps = writeUpRepository.findByUserAndCompetition(userId, competitionId);
        WriteUp writeUp;
        
        if (!existingWriteUps.isEmpty()) {
            // 如果已存在，更新最新的WriteUp（保留最新一版）
            writeUp = existingWriteUps.get(0); // 取第一个（通常只有一个）
            writeUp.setTitle(title);
            writeUp.setContent(content);
            writeUp.setCreateTime(java.time.LocalDateTime.now()); // 更新创建时间
        } else {
            // 如果不存在，创建新的WriteUp
            writeUp = new WriteUp(user, competition, title, content);
        }
        
        return writeUpRepository.save(writeUp);
    }

    @Override
    public Optional<WriteUp> getWriteUpById(Integer writeUpId) {
        return writeUpRepository.findById(writeUpId);
    }

    @Override
    public List<WriteUp> getWriteUpsByUser(Integer userId) {
        return writeUpRepository.findByUser_UserID(userId);
    }

    @Override
    public List<WriteUp> getWriteUpsByCompetition(Integer competitionId) {
        return writeUpRepository.findByCompetition_CompetitionID(competitionId);
    }

    @Override
    public List<WriteUp> getWriteUpsByUserAndCompetition(Integer userId, Integer competitionId) {
        return writeUpRepository.findByUserAndCompetition(userId, competitionId);
    }

    @Override
    public List<WriteUp> searchWriteUpsByTitle(String keyword) {
        return writeUpRepository.findByTitleContaining(keyword);
    }

    @Override
    public WriteUp updateWriteUp(WriteUp writeUp) {
        // 验证WriteUp是否存在
        if (writeUp.getWriteUpID() == null || !writeUpRepository.existsById(writeUp.getWriteUpID())) {
            throw new IllegalArgumentException("解题报告不存在");
        }

        validateWriteUp(writeUp);
        return writeUpRepository.save(writeUp);
    }

    @Override
    public void deleteWriteUp(Integer writeUpId) {
        if (!writeUpRepository.existsById(writeUpId)) {
            throw new IllegalArgumentException("解题报告不存在");
        }
        writeUpRepository.deleteById(writeUpId);
    }

    @Override
    public boolean existsWriteUpByUserAndCompetition(Integer userId, Integer competitionId) {
        List<WriteUp> writeUps = writeUpRepository.findByUserAndCompetition(userId, competitionId);
        return !writeUps.isEmpty();
    }

    /**
     * 验证解题报告数据的有效性
     */
    private void validateWriteUp(WriteUp writeUp) {
        if (writeUp.getUser() == null || writeUp.getUser().getUserID() == null) {
            throw new IllegalArgumentException("用户信息不能为空");
        }

        if (writeUp.getCompetition() == null || writeUp.getCompetition().getCompetitionID() == null) {
            throw new IllegalArgumentException("竞赛信息不能为空");
        }

        if (writeUp.getTitle() == null || writeUp.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("报告标题不能为空");
        }

        if (writeUp.getContent() == null || writeUp.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("报告内容不能为空");
        }

        if (writeUp.getTitle().length() > 100) {
            throw new IllegalArgumentException("报告标题长度不能超过100个字符");
        }
    }
}