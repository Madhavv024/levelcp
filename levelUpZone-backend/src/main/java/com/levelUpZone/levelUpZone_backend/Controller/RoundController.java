package com.levelUpZone.levelUpZone_backend.Controller;


import com.levelUpZone.levelUpZone_backend.DTO.UserRoundDTO;
import com.levelUpZone.levelUpZone_backend.Service.RoundLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/round")
public class RoundController {


    @Autowired
    RoundLogic roundLogic;

    @PostMapping("createRound")
    public UserRoundDTO createRound(@RequestBody UserRoundDTO userRoundDTO){
        return roundLogic.createRound(userRoundDTO);
    }

    // check submission and complete round
    @PutMapping("roundSubmission")
    public UserRoundDTO roundSubmission(@RequestBody UserRoundDTO userRoundDTO){
        return null;
    }

    @GetMapping("getPreviousRound")
    public List<UserRoundDTO> getPreviousRound(@RequestParam("userId") Long userId){
        return roundLogic.getPreviousRound(userId);
    }
}
