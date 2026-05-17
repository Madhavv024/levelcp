package com.levelUpZone.levelUpZone_backend.Config;

import com.levelUpZone.levelUpZone_backend.DAO.UserDAO;
import com.levelUpZone.levelUpZone_backend.Exception.CustomAccessDeniedHandler;
import com.levelUpZone.levelUpZone_backend.Exception.CustomAuthenticationEntryPoint;
import com.levelUpZone.levelUpZone_backend.Security.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    private final UserDAO userDAO;

    public SecurityConfig(JwtFilter jwtFilter, UserDAO userDAO, OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.jwtFilter = jwtFilter;
        this.userDAO = userDAO;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(c -> {
                })
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                ).formLogin(form -> form.disable())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                        .accessDeniedHandler(new CustomAccessDeniedHandler())
                )
                .logout(logout -> logout.disable())
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2SuccessHandler)
                )
                .httpBasic(httpBasic -> httpBasic.disable())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> (UserDetails) userDAO.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username)
                );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
