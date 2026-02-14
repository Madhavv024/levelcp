package com.levelUpZone.levelUpZone_backend.Service.Impl;

import com.levelUpZone.levelUpZone_backend.DAO.*;
import com.levelUpZone.levelUpZone_backend.DTO.ContestHistory;
import com.levelUpZone.levelUpZone_backend.DTO.ProblemDTO;
import com.levelUpZone.levelUpZone_backend.DTO.Request.JabardastRequest;
import com.levelUpZone.levelUpZone_backend.DTO.Response.JabardastResponse;
import com.levelUpZone.levelUpZone_backend.Entity.*;
import com.levelUpZone.levelUpZone_backend.Service.RoundSubLogic;
import com.levelUpZone.levelUpZone_backend.Service.UserLogic;
import com.levelUpZone.levelUpZone_backend.Util.CFUtility;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class UserServiceLogicImpl implements UserLogic {


    @Autowired
    UserDAO userDAO;

    @Autowired
    CFUtility  cfutility;

    @Autowired
    UserRoundResultDAO userRoundResultDAO;

    @Autowired
    UserProblemMapDAO userProblemMapDAO;

    @Autowired
    RoundProblemMapDAO roundProblemMapDAO;

    @Autowired
    CodeforcesProblemDAO codeforcesProblemDAO;

    @Autowired
    RoundSubLogic roundSubLogic;

    @Override
    public Optional<UserEntity> checkUserExist(String email){
        Optional<UserEntity> userEntityOp = userDAO.findByEmail(email);
        return userEntityOp;
    }

    @Override
    public Optional<UserEntity> checkUserExist(Long userId){
        Optional<UserEntity> userEntityOp = userDAO.findById(userId);
        return userEntityOp;
    }

    @Override
    public List<JabardastResponse> getAllUsers() {
        List<UserEntity> list = userDAO.findAll();
        List<JabardastResponse> jabardastResponses = new ArrayList<>();

        list.forEach(userEntity -> {
            JabardastResponse jabardastResponse = new JabardastResponse();
            jabardastResponse.setName(userEntity.getUsername());
            jabardastResponse.setEmail(userEntity.getEmail());
            jabardastResponses.add(jabardastResponse);
        });
        return jabardastResponses;
    }

    @Override
    public void fetchAndUpdateProblems(JabardastRequest request) {
        try {
            cfutility.fetchAndUpdateProblems();
        }catch (RuntimeException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Iterable<UserProblemMapEntity> getExistingProblem(Long userId) {
        Specification<UserProblemMapEntity> specification = problemMapEntitySpecification(userId).and(isActive());
        return userProblemMapDAO.findAll(specification);
    }

    @Override
    public Integer getSuggestedUserLevel(Long userId){
        Optional<UserEntity> userEntityOp = userDAO.findById(userId);
        if(userEntityOp.isPresent()){
            UserEntity userEntity = userEntityOp.get();
            Integer leve = userEntity.getCurrentLevelId() + 1; // suggest next level
            return leve;
        }else{
            throw new RuntimeException(userEntityOp.get().getUsername()); // user not logged-in
        }
    }

    @Override
    public void saveUserProblems(List<UserProblemMapEntity> userProblemMapEntityLs) {
        userProblemMapDAO.saveAll(userProblemMapEntityLs);
    }

    private static Specification<UserProblemMapEntity> isActive() {
        return (root, query, cb) ->
                cb.isTrue(root.get("active"));
    }

    private static Specification<UserProblemMapEntity> problemMapEntitySpecification(Long userId) {
        return (root, query, cb) ->
                cb.equal(root.get("userId"), userId);
    }

    @Override
    public List<ContestHistory> getUserContestHistory(Long userId) {
        Optional<UserEntity> userEntityOp = userDAO.findById(userId);
        if(userEntityOp.isPresent()){
            // fetch results from user round result
            List<RoundEntity> roundEntities = roundSubLogic.getUserRounds(userId);
            if(roundEntities.isEmpty()){
                throw new RuntimeException("No rounds found, Please take part in contest");
            }

            Iterable<UserRoundResultEntity> userRoundResultEntities = userRoundResultDAO.findByUserId(userId);

            Map<Long, UserRoundResultEntity>  roundResultEntityMapByRoundId = StreamSupport.stream(userRoundResultEntities.spliterator(), false)
                    .collect(Collectors.toMap(UserRoundResultEntity::getRoundId, userRoundResultEntity -> userRoundResultEntity));

            List<ContestHistory> contestHistories = new ArrayList<>();

            Set<Long> roundIds = StreamSupport.stream(userRoundResultEntities.spliterator(), false).map(
                    UserRoundResultEntity::getRoundId).collect(Collectors.toSet());

            List<RoundProblemMapEntity> roundProblemMapEntities = roundProblemMapDAO.findAllById(roundIds);

            Set<Long> problemIds = roundProblemMapEntities.stream().map(RoundProblemMapEntity::getProblemId)
                    .map(Integer::longValue).collect(Collectors.toSet());

            List<CodeforcesProblemEntity> codeforcesProblemEntities = codeforcesProblemDAO.findAllById(problemIds);

            Map<Long, CodeforcesProblemEntity> codeforcesProblemEntityMap = codeforcesProblemEntities.stream().collect(Collectors.toMap(CodeforcesProblemEntity::getId, Function.identity()));

            Map<Long, List<RoundProblemMapEntity>> roundProblemMap =  StreamSupport.stream(roundProblemMapEntities.spliterator(), false)
                    .collect(Collectors.groupingBy(RoundProblemMapEntity::getRoundId));

            roundEntities.forEach(roundEntity -> {
                ContestHistory contestHistory = new ContestHistory();
                UserRoundResultEntity userRoundResultEntity = roundResultEntityMapByRoundId.get(roundEntity.getId());
                contestHistory.setDate(roundEntity.getCreatedAt());
                contestHistory.setTopic(roundEntity.getTopic());
                contestHistory.setId(roundEntity.getId());
                contestHistory.setLevel(roundEntity.getLevelId().intValue());

                List<RoundProblemMapEntity> problemMapEntities = roundProblemMap.get(roundEntity.getId());
                List<ProblemDTO> problemDTOs = new ArrayList<>();

                problemMapEntities.forEach(problemMapEntity -> {
                    ProblemDTO problemDTO = new ProblemDTO();
                    CodeforcesProblemEntity  codeforcesProblemEntity = codeforcesProblemEntityMap.get(problemMapEntity.getId());
                    problemDTO.setContestId(codeforcesProblemEntity.getCfContestId());
                    problemDTO.setProblemInd(codeforcesProblemEntity.getCfProblemId());
                    problemDTOs.add(problemDTO);
                });
                contestHistory.setProblems(problemDTOs);

                contestHistories.add(contestHistory);
            });
            return contestHistories;
        }else{
            throw new RuntimeException(userId+"");
        }
    }
}
