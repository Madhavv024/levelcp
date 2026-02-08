package com.jabardastcoder.jabardastcoder_backend.Service;

import com.jabardastcoder.jabardastcoder_backend.Entity.CodeforcesProblemEntity;
import com.jabardastcoder.jabardastcoder_backend.Entity.RoundEntity;
import com.jabardastcoder.jabardastcoder_backend.Entity.RoundProblemMapEntity;

import java.util.List;
import java.util.Set;

public interface RoundSubLogic {
    Iterable<CodeforcesProblemEntity> getContestProblems(Integer minRating, Integer maxRating);

    RoundEntity saveUserRoundEntity(RoundEntity roundsEntity);

    void saveRoundProblemMap(List<RoundProblemMapEntity> roundProblemMapEntityLs);

    List<RoundEntity> getUserRounds(Long userId);

    List<CodeforcesProblemEntity> getProblemsByContestId(Set<Integer> contestId);
}
