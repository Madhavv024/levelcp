package com.levelUpZone.levelUpZone_backend.DAO;

import com.levelUpZone.levelUpZone_backend.Entity.LevelsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LevelsDAO extends JpaRepository<LevelsEntity, Long> {

    Optional<LevelsEntity> findByLevelNumber(Integer levelNumber);
}
