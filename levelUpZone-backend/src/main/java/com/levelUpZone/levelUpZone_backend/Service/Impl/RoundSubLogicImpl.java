package com.levelUpZone.levelUpZone_backend.Service.Impl;

import com.levelUpZone.levelUpZone_backend.DAO.RoundDAO;
import com.levelUpZone.levelUpZone_backend.DAO.RoundProblemMapDAO;
import com.levelUpZone.levelUpZone_backend.Entity.CodeforcesProblemEntity;
import com.levelUpZone.levelUpZone_backend.DAO.CodeforcesProblemDAO;
import com.levelUpZone.levelUpZone_backend.Entity.RoundEntity;
import com.levelUpZone.levelUpZone_backend.Entity.RoundProblemMapEntity;
import com.levelUpZone.levelUpZone_backend.Service.RoundSubLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RoundSubLogicImpl implements RoundSubLogic {


    @Autowired
    CodeforcesProblemDAO codeforcesProblemDAO;

    @Autowired
    RoundDAO roundDAO;

    @Autowired
    RoundProblemMapDAO roundProblemMapDAO;

    @Override
    public Iterable<CodeforcesProblemEntity> getContestProblems(Integer minRating, Integer maxRating) {
        Specification<CodeforcesProblemEntity> specification = hasRatingBetween(minRating, maxRating).and(isActive());
        return codeforcesProblemDAO.findAll(specification);
    }

    private static Specification<CodeforcesProblemEntity> hasRatingBetween(Integer min, Integer max) {

        return (root, query, cb) -> {
            if (min == null || max == null) return null;
            return cb.between(root.get("problemRating"), min, max);
        };
    }

    private static Specification<CodeforcesProblemEntity> isActive() {
        return (root, query, cb) ->
                cb.isTrue(root.get("active"));
    }

    private static Specification<CodeforcesProblemEntity> hasTag(String tag) {
        return (root, query, cb) ->
                cb.like(root.get("problemTags"), "%" + tag + "%");
    }

    private static Specification<CodeforcesProblemEntity> hasContestId(Set<Integer> contestIds) {
        return (root, query, cb) -> {
            if (contestIds == null || contestIds.isEmpty()) {
                return cb.disjunction();
            }
            return root.get("cfContestId").in(contestIds);
        };
    }

    @Override
    public RoundEntity saveUserRoundEntity(RoundEntity roundsEntity) {
        return roundDAO.save(roundsEntity);
    }

    @Override
    public void saveRoundProblemMap(List<RoundProblemMapEntity> roundProblemMapEntityLs) {
        roundProblemMapDAO.saveAll(roundProblemMapEntityLs);
    }

    @Override
    public List<RoundEntity> getUserRounds(Long userId) {
        return roundDAO.findAllByUserId(userId);
    }

    @Override
    public List<CodeforcesProblemEntity> getProblemsByContestId(Set<Integer> contestId)
    {
        Specification<CodeforcesProblemEntity> specification = hasContestId(contestId);
        return codeforcesProblemDAO.findAll(specification);
    }
}
