package com.todo.todoapplication.filter;

import com.todo.todoapplication.utils.JwtUtils;
import com.todo.todoapplication.utils.RedisUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RedisUtils redisUtils;

    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        // 1. 检查请求头是否包含 Token
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                // 2. 本地验签（检查有没有被篡改、是否过期）
                if (!jwtUtils.isExpired(token)) {

                    // === 🌟 Redis 核心校验逻辑 开始 🌟 ===
                    // 我们约定，登录成功后存入 Redis 的 key 格式为: "auth:token:具体的token字符串"
                    String redisKey = "auth:token:" + token;
                    Object objFromRedis = redisUtils.get(redisKey);

                    if (objFromRedis != null) {
                        String usernameFromRedis = jwtUtils.getUsername(token);

                        // 💡 核心优化：既然 Redis 里有，说明用户绝对在登录状态！
                        // 直接在内存里“捏”一个简单的 UserDetails 出来，彻底抛弃 userDetailsService 查库操作！
                        UserDetails userDetails = org.springframework.security.core.userdetails.User
                                .withUsername(usernameFromRedis)
                                .password("") // 这里的密码已经不需要了，随便填空字符串即可
                                .authorities(new ArrayList<>()) // 没有复杂的角色权限，传空列表
                                .build();

                        // 颁发内部通行证
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        // Redis 里找不到，说明已登出或过期
                        logger.warn("Token is valid but not found in Redis. Possibly logged out.");
                    }
                    // === 🌟 Redis 核心校验逻辑 结束 🌟 ===
                }
            } catch (Exception e) {
                logger.error("Token verification failed: " + e.getMessage());
            }
        }

        // 无论如何，让请求继续往下走（没拿到通行证的会在门禁处被拦截）
        chain.doFilter(request, response);
    }
}
