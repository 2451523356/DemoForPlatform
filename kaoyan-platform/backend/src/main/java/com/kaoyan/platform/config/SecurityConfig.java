package com.kaoyan.platform.config;

import com.kaoyan.platform.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/news/**").permitAll()
                .antMatchers("/api/resource/**").permitAll()
                .antMatchers("/api/resource-comment/list/**").permitAll()
                .antMatchers("/api/course/list").permitAll()
                .antMatchers("/api/course/detail/**").permitAll()
                .antMatchers("/api/course/chapters/**").permitAll()
                .antMatchers("/api/course/chapter/**").permitAll()
                .antMatchers("/api/course/reviews/**").permitAll()
                .antMatchers("/api/community/posts").permitAll()
                .antMatchers("/api/community/post/**").permitAll()
                .antMatchers("/api/community/circles").permitAll()
                .antMatchers("/api/community/circle/**").permitAll()
                .antMatchers("/api/live/list").permitAll()
                .antMatchers("/api/live/detail/**").permitAll()
                .antMatchers("/api/live/danmaku/**").permitAll()
                .antMatchers("/api/live/upcoming").permitAll()
                .antMatchers("/api/practice/questions").permitAll()
                .antMatchers("/api/practice/question/**").permitAll()
                .antMatchers("/api/politics/contents").permitAll()
                .antMatchers("/api/politics/content/**").permitAll()
                .antMatchers("/api/politics/chapter-stats").permitAll()
                .antMatchers("/api/writing/templates").permitAll()
                .antMatchers("/api/writing/template/**").permitAll()
                .antMatchers("/api/checkin/leaderboard").permitAll()
                .antMatchers("/api/school/**").permitAll()
                .antMatchers("/api/adjustment/**").permitAll()
                .antMatchers("/api/plan-template/list").permitAll()
                .antMatchers("/api/plan-template/detail/**").permitAll()
                .antMatchers("/api/recommendation/**").permitAll()
                .antMatchers("/api/national-score/**").permitAll()
                .antMatchers("/api/notification/unread-count").permitAll()
                .antMatchers("/api/upload/**").permitAll()
                .antMatchers("/avatars/**").permitAll()
                .antMatchers("/videos/**").permitAll()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
