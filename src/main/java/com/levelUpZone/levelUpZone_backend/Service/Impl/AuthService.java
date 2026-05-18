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
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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

    public LoginResponse register(LoginRequest request , HttpServletResponse response) {

        if (userDAO.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setVersion(1);
        user.setProvider("form");
        user.setCreatedAt(OffsetDateTime.now());

        UserEntity saved = userDAO.save(user);

        String token = jwtUtil.generateToken(saved, "ACCESS");
        String refreshToken = jwtUtil.generateToken(saved, "REFRESH");

        // Set refresh token in HttpOnly cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)        // true in production (HTTPS)
                .path("/")
                .sameSite("Lax")      // works for localhost cross-port
                .maxAge(3 * 24 * 60 * 60) // 3 days
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return new LoginResponse(token, saved.getEmail(), saved.getId().intValue(), saved.getCodeforcesHandle());
    }

    public LoginResponse login(LoginRequest request, HttpServletResponse response) {

        UserEntity user = userDAO.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user, "ACCESS");
        String refreshToken = jwtUtil.generateToken(user, "REFRESH");

        // 🔥 Set refresh token in HttpOnly cookie
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)      // true in production (HTTPS)
                .path("/")
                .sameSite("Lax")    // Lax works for localhost cross-port
                .maxAge(7 * 24 * 60 * 60) // 7 days
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());


        return new LoginResponse(token, user.getEmail(), user.getId().intValue(), user.getCodeforcesHandle());
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
                userDAO.save(user);
                String token = jwtUtil.generateToken(user, "REFRESH");
                return new LoginResponse(token, user.getEmail(), user.getId().intValue(), user.getCodeforcesHandle());
            } catch (HttpServerErrorException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ResponseEntity<?> createRefreshToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || !jwtUtil.isTokenValid(refreshToken , "REFRESH")) {
            clearRefreshTokenCookie(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token invalid or expired. Please login again.");
        }

        String username = jwtUtil.getEmailFromToken(refreshToken);
        UserEntity user = userDAO.findByEmail(username).orElse(null);

        if (user == null) {
            clearRefreshTokenCookie(response);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found. Please login again.");
        }

        String newAccessToken = jwtUtil.generateToken(user, "ACCESS");
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // true in production
                .path("/")
                .maxAge(0) // delete immediately
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }

}
