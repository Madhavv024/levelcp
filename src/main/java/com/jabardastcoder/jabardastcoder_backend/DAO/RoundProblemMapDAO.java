package com.jabardastcoder.jabardastcoder_backend.DAO;

import com.jabardastcoder.jabardastcoder_backend.Entity.RoundProblemMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Set;

public interface RoundProblemMapDAO extends JpaRepository<RoundProblemMapEntity, Long>, JpaSpecificationExecutor<RoundProblemMapEntity> {

    List<RoundProblemMapEntity> findByRoundIds(Set<Long> roundId);
}
