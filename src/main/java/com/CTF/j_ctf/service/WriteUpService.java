package com.CTF.j_ctf.service;

import com.CTF.j_ctf.entity.WriteUp;
import java.util.List;
import java.util.Optional;

public interface WriteUpService {

    WriteUp createWriteUp(WriteUp writeUp);

    WriteUp createWriteUp(Integer userId, Integer competitionId, String title, String content);

    Optional<WriteUp> getWriteUpById(Integer writeUpId);

    List<WriteUp> getWriteUpsByUser(Integer userId);

    List<WriteUp> getWriteUpsByCompetition(Integer competitionId);

    List<WriteUp> getWriteUpsByUserAndCompetition(Integer userId, Integer competitionId);

    List<WriteUp> searchWriteUpsByTitle(String keyword);

    WriteUp updateWriteUp(WriteUp writeUp);

    void deleteWriteUp(Integer writeUpId);

    boolean existsWriteUpByUserAndCompetition(Integer userId, Integer competitionId);
}