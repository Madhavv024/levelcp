package com.jabardastcoder.jabardastcoder_backend.DAO;

import com.jabardastcoder.jabardastcoder_backend.Entity.UserRoundResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRoundResultDAO extends JpaRepository<UserRoundResultEntity, Long>, JpaSpecificationExecutor<UserRoundResultEntity> {

    Iterable<UserRoundResultEntity> findByUserId(Long userId);

}
