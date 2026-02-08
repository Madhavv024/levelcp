package com.jabardastcoder.jabardastcoder_backend.DAO;

import com.jabardastcoder.jabardastcoder_backend.Entity.CodeforcesProblemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CodeforcesProblemDAO extends JpaRepository<CodeforcesProblemEntity, Long> , JpaSpecificationExecutor<CodeforcesProblemEntity> {

    Optional<CodeforcesProblemEntity> findByCfContestIdAndCfProblemId(Integer contestId, String index);
    List<CodeforcesProblemEntity> findAllByIds(Set<Long> ids);

}
