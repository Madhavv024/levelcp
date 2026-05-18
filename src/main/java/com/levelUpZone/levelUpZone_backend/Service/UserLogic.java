package com.levelUpZone.levelUpZone_backend.Service;


import com.levelUpZone.levelUpZone_backend.DTO.ContestHistory;
import com.levelUpZone.levelUpZone_backend.DTO.Levels;
import com.levelUpZone.levelUpZone_backend.DTO.Request.JabardastRequest;
import com.levelUpZone.levelUpZone_backend.DTO.Response.LevelUpZoneResponse;
import com.levelUpZone.levelUpZone_backend.Entity.UserEntity;
import com.levelUpZone.levelUpZone_backend.Entity.UserProblemMapEntity;

import java.util.List;
import java.util.Optional;

public interface UserLogic {


    Optional<UserEntity> checkUserExist(String email);

    Optional<UserEntity> checkUserExist(Long userId);

    List<LevelUpZoneResponse> getAllUsers();

    void fetchAndUpdateProblems(JabardastRequest request);


    Iterable<UserProblemMapEntity> getExistingProblem(Long id);

    Integer getSuggestedUserLevel(Long userId);

    void saveUserProblems(List<UserProblemMapEntity> userProblemMapEntityLs);

    List<ContestHistory> getUserContestHistory(Long userId);

    LevelUpZoneResponse getUserDetails();

    List<Levels> getAllLevels();
}
