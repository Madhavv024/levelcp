package com.jabardastcoder.jabardastcoder_backend.Controller;


import com.jabardastcoder.jabardastcoder_backend.DTO.ContestHistory;
import com.jabardastcoder.jabardastcoder_backend.DTO.Request.JabardastRequest;
import com.jabardastcoder.jabardastcoder_backend.DTO.Response.JabardastResponse;
import com.jabardastcoder.jabardastcoder_backend.DTO.UserRoundDTO;
import com.jabardastcoder.jabardastcoder_backend.Service.UserLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class JabardastController {


    @Autowired
    UserLogic userLogic;


    @GetMapping("getAllUsers")
    public List<JabardastResponse> getAllUsers()
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


}
