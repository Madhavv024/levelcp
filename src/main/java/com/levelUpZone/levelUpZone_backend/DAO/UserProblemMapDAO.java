package com.levelUpZone.levelUpZone_backend.DAO;

import com.levelUpZone.levelUpZone_backend.Entity.UserProblemMapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserProblemMapDAO extends JpaRepository<UserProblemMapEntity, Long>, JpaSpecificationExecutor<UserProblemMapEntity> {
}
