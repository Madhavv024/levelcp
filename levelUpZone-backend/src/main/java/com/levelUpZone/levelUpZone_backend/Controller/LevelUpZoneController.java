package com.levelUpZone.levelUpZone_backend.Controller;


import com.levelUpZone.levelUpZone_backend.DTO.ContestHistory;
import com.levelUpZone.levelUpZone_backend.DTO.Levels;
import com.levelUpZone.levelUpZone_backend.DTO.Request.JabardastRequest;
import com.levelUpZone.levelUpZone_backend.DTO.Response.LevelUpZoneResponse;
import com.levelUpZone.levelUpZone_backend.Service.RoundLogic;
import com.levelUpZone.levelUpZone_backend.Service.UserLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class LevelUpZoneController {


    @Autowired
    UserLogic userLogic;

    @Autowired
    RoundLogic roundLogic;


    @GetMapping("getAllUsers")
    public List<LevelUpZoneResponse> getAllUsers()
    {
        return userLogic.getAllUsers();
    }

    @PostMapping("fetchAndUpdateProblems")
    public ResponseEntity<String> fetchAndUpdateProblems(@RequestBody JabardastRequest request) {
        userLogic.fetchAndUpdateProblems(request);
        return new ResponseEntity<>("Data Updated Successfully", HttpStatus.OK);
    }

    @GetMapping("getUserSuggestedLevel")
    public Integer getUserSuggestedLevel(@RequestParam Long userId)
    {
        return userLogic.getSuggestedUserLevel(userId);
    }

    @GetMapping("getUserContestHistory")
    public List<ContestHistory> getUserContestHistory(@RequestParam Long userId){
        return userLogic.getUserContestHistory(userId);
    }

    @GetMapping("details")
    public LevelUpZoneResponse details(){
        return userLogic.getUserDetails();
    }

    @GetMapping("levels")
    public List<String> levels(){
        return List.of("level1",  "level2", "level3", "level4", "level5");
    }


}
