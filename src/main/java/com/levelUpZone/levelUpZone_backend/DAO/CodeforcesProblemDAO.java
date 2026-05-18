package com.levelUpZone.levelUpZone_backend.DAO;

import com.levelUpZone.levelUpZone_backend.Entity.CodeforcesProblemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CodeforcesProblemDAO extends JpaRepository<CodeforcesProblemEntity, Long> , JpaSpecificationExecutor<CodeforcesProblemEntity> {

    Optional<CodeforcesProblemEntity> findByCfContestIdAndCfProblemId(Integer contestId, String index);

}
