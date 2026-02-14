package com.levelUpZone.levelUpZone_backend.Controller;


import com.levelUpZone.levelUpZone_backend.DTO.Request.LoginRequest;
import com.levelUpZone.levelUpZone_backend.DTO.Response.LoginResponse;
import com.levelUpZone.levelUpZone_backend.Service.Impl.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    // REGISTER
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public LoginResponse register(@RequestBody LoginRequest request) {
        return authService.register(request);
    }

    // LOGIN
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PutMapping("/addCfHandle")
    public LoginResponse addCfHandle(@RequestBody LoginRequest request){
        return authService.addCfHandle(request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(value = "refreshToken", required = false) String refreshToken) {

        return authService.createRefreshToken(refreshToken);

    }

}
