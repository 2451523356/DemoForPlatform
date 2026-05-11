package com.kaoyan.platform.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.kaoyan.platform.entity.User;
import com.kaoyan.platform.service.UserService;
import com.kaoyan.platform.utils.JwtUtil;
import com.kaoyan.platform.utils.UserContext;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserService userService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token)) {
            try {
                DecodedJWT jwt = jwtUtil.verifyToken(token);
                Long userId = Long.valueOf(jwt.getSubject());
                String username = jwt.getClaim("username").asString();
                String role = jwt.getClaim("role").asString();
                
                User user = userService.getById(userId);
                if (user != null) {
                    UserContext.setUser(user);
                    
                    // 创建角色列表
                    List<String> roles = new ArrayList<>();
                    roles.add("ROLE_" + role);
                    
                    // 创建权限列表
                    List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();
                    for (String r : roles) {
                        authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority(r));
                    }
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.error("JWT验证失败: {}", e.getMessage());
                // Token 过期或无效，返回 401 错误
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"code\": 401, \"message\": \"登录已过期，请重新登录\"}");
                return;
            }
        }
        
        filterChain.doFilter(request, response);
        
        // 在请求处理完成后移除 UserContext
        UserContext.remove();
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // 支持通过URL参数传递token（用于文件预览等新窗口场景）
        String queryToken = request.getParameter("token");
        if (StringUtils.hasText(queryToken)) {
            return queryToken;
        }
        return null;
    }
}
