package com.levelUpZone.levelUpZone_backend.Security;

import com.levelUpZone.levelUpZone_backend.Config.JwtUtil;
import com.levelUpZone.levelUpZone_backend.DAO.UserDAO;
import com.levelUpZone.levelUpZone_backend.Entity.UserEntity;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {


    private final JwtUtil jwtUtil;
    private final UserDAO userDAO;

    public OAuth2SuccessHandler(JwtUtil jwtUtil, UserDAO userDAO) {
        this.jwtUtil = jwtUtil;
        this.userDAO = userDAO;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken =
                (OAuth2AuthenticationToken) authentication;

        OAuth2User oauthUser = oauthToken.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        // 1. Find or create user
        UserEntity user = userDAO.findByEmail(email)
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setEmail(email);
                    newUser.setUsername(name);
                    newUser.setPassword("password");
                    newUser.setActive(true);
                    newUser.setVersion(1);
                    newUser.setProvider("Google");
                    return userDAO.save(newUser);
                });

        // 2. Generate tokens using YOUR method
        String accessToken = jwtUtil.generateToken(user, "access");
        String refreshToken = jwtUtil.generateToken(user, "refresh");

// Store refresh token in HttpOnly cookie
        ResponseCookie refreshCookie = ResponseCookie.from(
                        "refreshToken",
                        refreshToken
                )
                .httpOnly(true)
                .secure(false) // true in production HTTPS
                .path("/")
                .maxAge(60 * 60 * 24) // 1 day
                .sameSite("Lax")
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString()
        );

// Redirect ONLY with access token
        response.sendRedirect(
                "http://localhost:5173/oauth-success?access=" + accessToken
        );
    }
}