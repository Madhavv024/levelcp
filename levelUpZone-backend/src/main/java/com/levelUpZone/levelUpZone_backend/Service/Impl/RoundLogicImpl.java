package com.levelUpZone.levelUpZone_backend.Service.Impl;

import com.levelUpZone.levelUpZone_backend.Config.UserContext;
import com.levelUpZone.levelUpZone_backend.DTO.UserRoundDTO;
import com.levelUpZone.levelUpZone_backend.DAO.LevelsDAO;
import com.levelUpZone.levelUpZone_backend.Entity.*;
import com.levelUpZone.levelUpZone_backend.Exception.ResourceNotFoundException;
import com.levelUpZone.levelUpZone_backend.Service.RoundLogic;
import com.levelUpZone.levelUpZone_backend.Service.UserLogic;
import com.levelUpZone.levelUpZone_backend.Util.RoundStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
@Transactional
public class RoundLogicImpl implements RoundLogic {


    @Autowired
    UserLogic userLogic;

    @Autowired
    LevelsDAO levelsDAO;

    @Autowired
    RoundSubLogicImpl roundSubLogic;

    @Override
    public UserRoundDTO createRound(UserRoundDTO userRoundDTO) {
        /*
User
 → Start Round
 → Problems assigned
 → Solve on Codeforces
 → Fetch submissions
 → Evaluate round
 → Update level
        * */
        try {
            Optional<UserEntity> userEntityOp = userLogic.checkUserExist(userRoundDTO.getUserId());

            if(userEntityOp.isPresent()){
                // user registered
                UserEntity userEntity = userEntityOp.get();
                String cfHandle = userEntity.getCodeforcesHandle();
                if(Objects.nonNull(cfHandle)){
                    // fetch current rating and maxrating from level id
                    Integer levelId = userRoundDTO.getLevelId().intValue();
                    // fetch min - max rating on the basis of user current level
                    Optional<LevelsEntity> levelsEntityOp = levelsDAO.findByLevelNumber(levelId);

                    Integer minRating = levelsEntityOp.get().getMinRating(),
                            maxRating = levelsEntityOp.get().getMaxRating();

                    // fetch the problems in range of rating
                    Iterable<CodeforcesProblemEntity> cfProblems = roundSubLogic.getContestProblems(minRating, maxRating);
                    // fetch and filter the problems which are already mapped with user to avoid that
                    Iterable<UserProblemMapEntity> userProblemMapEntities = userLogic.getExistingProblem(userEntity.getId());
                    Set<String> userExistingProblems = StreamSupport.stream(userProblemMapEntities.spliterator(), false)
                            .map( ent ->
                                    {
                                        return ent.getContestId().toString() + "~" + ent.getProblemId();
                                    }
                            ).collect(Collectors.toSet());

                    Map<String, CodeforcesProblemEntity> codeforcesProblemEntityMap = StreamSupport.stream(cfProblems.spliterator(), false)
                            .filter(ent -> !userExistingProblems.contains(ent.getCfContestId()+"~"+ent.getId()))
                            .collect(Collectors.toMap(
                                    ent -> {
                                        String key = ent.getCfContestId() + "~" + ent.getCfProblemId();
                                        return key;
                                    }, Function.identity(), (a , b) ->
                                            a.getCfProblemSolvedCount() >= b.getCfProblemSolvedCount() ?
                                                    a : b
                            ));
                    List<CodeforcesProblemEntity> problemsForNewContest = new ArrayList<>();
                    // get N problems N-1 easy and 1 medium according to solve count

                    Integer totalSolveCount = codeforcesProblemEntityMap.values().stream()
                            .mapToInt(CodeforcesProblemEntity::getCfProblemSolvedCount).sum();

                    Integer totalProblems = codeforcesProblemEntityMap.size();

                    int averageSolve =  totalSolveCount / totalProblems;

                    int counter = 1;
                    for(Map.Entry<String, CodeforcesProblemEntity> entry : codeforcesProblemEntityMap.entrySet()){
                        if(counter < userRoundDTO.getQuestionCount()){
                            // add new problems
                            problemsForNewContest.add(entry.getValue());
                            counter++;
                            continue;
                        }
                        break;
                    }
                    // search for problem nearing to average solve count

                    List<CodeforcesProblemEntity> sortedProblemsForNewContest = new ArrayList<>(codeforcesProblemEntityMap.values());

                    sortedProblemsForNewContest.sort(Comparator.comparing(CodeforcesProblemEntity::getCfProblemSolvedCount));
                    CodeforcesProblemEntity averageSolveCountProblem = findAverageSolveCountProblem(sortedProblemsForNewContest, averageSolve);
                    problemsForNewContest.add(averageSolveCountProblem);
                    // create user round map, user problem map, and return user round dto with start time.
                    userRoundDTO = createUserRound(userEntity , problemsForNewContest, levelId);

                    List<String> problemLinks = new ArrayList<>();
                    problemsForNewContest.forEach(problem -> {
                        problemLinks.add(buildCodeforcesProblemLink(problem));
                    });

                    userRoundDTO.setUserProblems(problemLinks);

                    return userRoundDTO;


                }else{
                    // prompt user to sync codeforces id and then try again
                    throw new ResourceNotFoundException("User not synced his codeforces accound");
                }

            }
            return null;
        }catch (Exception e){
            // user not registered and userid is null
            throw new ResourceNotFoundException(userRoundDTO.getUserId().toString());
        }
    }

    private UserRoundDTO createUserRound(UserEntity userEntity, List<CodeforcesProblemEntity> problemsForNewContest, Integer levelId) {
        UserRoundDTO userRoundDTO = new UserRoundDTO();
        userRoundDTO.setUserId(userEntity.getId());
        userRoundDTO.setUserName(userEntity.getUsername());
        userRoundDTO.setLevelId(levelId.longValue());
        userRoundDTO.setStartTime(OffsetDateTime.now());

        // create user round map
        RoundEntity roundsEntity = new RoundEntity();
        roundsEntity.setUserId(userEntity.getId());
        roundsEntity.setLevelId(levelId.longValue());
        roundsEntity.setStartTime(OffsetDateTime.now());
        roundsEntity.setStatus(RoundStatus.IN_PROGRESS.toString());
        roundsEntity.setActive(true);
        roundsEntity.setVersion(1);
        roundsEntity.setCreatedAt(OffsetDateTime.now());
        roundsEntity = roundSubLogic.saveUserRoundEntity(roundsEntity);

        // create round problem map,
        List<RoundProblemMapEntity> roundProblemMapEntityLs = new ArrayList<>();
        problemsForNewContest.forEach(problem -> {
            RoundProblemMapEntity roundProblemMapEntity = new RoundProblemMapEntity();
            roundProblemMapEntity.setRoundId(problem.getId());
            roundProblemMapEntity.setProblemId(problem.getId().intValue());
            roundProblemMapEntity.setActive(true);
            roundProblemMapEntity.setCreatedAt(OffsetDateTime.now());
            roundProblemMapEntityLs.add(roundProblemMapEntity);
        });
        roundSubLogic.saveRoundProblemMap(roundProblemMapEntityLs);


        // create user problem map
        List<UserProblemMapEntity>  userProblemMapEntityLs = new ArrayList<>();
        problemsForNewContest.forEach(problem -> {
            UserProblemMapEntity userProblemMapEntity = new UserProblemMapEntity();
            userProblemMapEntity.setUserId(userEntity.getId());
            userProblemMapEntity.setProblemId(problem.getId().intValue());
            userProblemMapEntity.setContestId(problem.getCfContestId());
            userProblemMapEntity.setActive(true);
            userProblemMapEntity.setUsedAt(OffsetDateTime.now());
            userProblemMapEntityLs.add(userProblemMapEntity);
        });
        userLogic.saveUserProblems(userProblemMapEntityLs);

        return userRoundDTO;
    }

    private String buildCodeforcesProblemLink(CodeforcesProblemEntity codeforcesProblemEntity){
        return "https://codeforces.com/contest/"+codeforcesProblemEntity.getCfContestId()+"/problem/"+codeforcesProblemEntity.getCfProblemId();
    }

    private CodeforcesProblemEntity findAverageSolveCountProblem(List<CodeforcesProblemEntity> codeforcesProblemEntities, int averageSolveCount){
        int low = 0;
        int high = codeforcesProblemEntities.size();
        CodeforcesProblemEntity codeforcesProblemEntity = codeforcesProblemEntities.get(0);
        while(low <= high){
            int mid = low + (high - low)/2;
            int solveCount = codeforcesProblemEntities.get(mid).getCfProblemSolvedCount();
            if(solveCount >= averageSolveCount){
                codeforcesProblemEntity = codeforcesProblemEntities.get(mid);
                high = mid - 1;
            }else
                low = mid + 1;
        }
        return codeforcesProblemEntity;
    }

    @Override
    public List<UserRoundDTO> getPreviousRound(Long userId) {
        List<UserRoundDTO> userRoundDTOList = new ArrayList<>();
        List<RoundEntity> roundEntities = roundSubLogic.getUserRounds(userId);
        roundEntities.forEach(roundEntity -> {
            UserRoundDTO userRoundDTO = new UserRoundDTO();
            userRoundDTO.setUserId(userId);
            userRoundDTO.setRoundId(roundEntity.getId());
            userRoundDTOList.add(userRoundDTO);
        });
        return userRoundDTOList;
    }
}
