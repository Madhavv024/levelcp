package com.levelUpZone.levelUpZone_backend.Service;

import com.levelUpZone.levelUpZone_backend.DTO.UserRoundDTO;

import java.util.List;

public interface RoundLogic {
    UserRoundDTO createRound(UserRoundDTO userRoundDTO);

    List<UserRoundDTO> getPreviousRound(Long userId);
}
