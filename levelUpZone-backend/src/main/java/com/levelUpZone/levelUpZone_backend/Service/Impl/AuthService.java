package com.levelUpZone.levelUpZone_backend.Service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.levelUpZone.levelUpZone_backend.Config.JwtUtil;
import com.levelUpZone.levelUpZone_backend.DTO.Request.LoginRequest;
import com.levelUpZone.levelUpZone_backend.DTO.Response.LoginResponse;
import com.levelUpZone.levelUpZone_backend.Entity.LevelsEntity;
import com.levelUpZone.levelUpZone_backend.Entity.UserEntity;
import com.levelUpZone.levelUpZone_backend.DAO.LevelsDAO;
import com.levelUpZone.levelUpZone_backend.DAO.UserDAO;
import com.levelUpZone.levelUpZone_backend.Exception.ResourceNotFoundException;
import com.levelUpZone.levelUpZone_backend.Exception.UnauthorizedException;
import com.levelUpZone.levelUpZone_backend.Util.CFUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Service
public class AuthService {

    private final UserDAO userDAO;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Value(value = "${cfURL}")
    private String cfUrl;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    LevelsDAO levelsDAO;

    @Autowired
    CFUtility cfUtility;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public LoginResponse register(LoginRequest request) {

        if (userDAO.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setVersion(1);
        user.setCreatedAt(OffsetDateTime.now());

        UserEntity saved = userDAO.save(user);

        String token = jwtUtil.generateToken(saved, "ACCESS");

        return new LoginResponse(token);
    }

    public LoginResponse login(LoginRequest request) {

        UserEntity user = userDAO.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user, "ACCESS");

        return new LoginResponse(token);
    }


    public LoginResponse addCfHandle(LoginRequest request) {
        Optional<UserEntity> userEntity = userDAO.findByEmail(request.getEmail());
        if(userEntity.isEmpty()){
            // throw error prompting user to login
            throw new RuntimeException("User not found");
        }else{
            UserEntity user = userEntity.get();
            // call api to get the data
            try {
                JsonNode root = null !=
                        cfUtility.getUserResponse(request.getCfHandle()).getBody() ? (JsonNode)cfUtility
                        .getUserResponse(request.getCfHandle()).getBody()
                        : null;

                if (root != null && "OK".equalsIgnoreCase(root.get("status").asText())) {

                    JsonNode userInfo = root.get("result").get(0);

                    int currentRating = userInfo.has("rating")
                            ? userInfo.get("rating").asInt()
                            : 0;

                    int maxRating = userInfo.has("maxRating")
                            ? userInfo.get("maxRating").asInt()
                            : 0;

                    // fetch level entity based on the current rating
                    Iterable<LevelsEntity> levelsEntities = levelsDAO.findAll();

                    LevelsEntity levelsEntity = StreamSupport.stream(levelsEntities.spliterator(), false).filter(
                            ent -> ent.getMinRating() <= currentRating && currentRating < ent.getMaxRating()
                            ).findFirst().get();

                    user.setCurrentLevelId(levelsEntity.getLevelNumber());
                    user.setVersion(user.getVersion() + 1);
                    user.setUpdatedAt(OffsetDateTime.now());

                }

                user.setCodeforcesHandle(request.getCfHandle());
                String token = jwtUtil.generateToken(user, "REFRESH");
                return new LoginResponse(token);
            } catch (HttpServerErrorException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ResponseEntity<?> createRefreshToken(String refreshToken) {
        if (refreshToken == null || !jwtUtil.isTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        String username = jwtUtil.getEmailFromToken(refreshToken);
        UserEntity user = userDAO.findByEmail(username).get();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        String newAccessToken = jwtUtil.generateToken(user, "REFRESH");
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}
