package com.todo.todoapplication.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类，提供 Token 的生成、解析、验证、刷新等常见操作。
 *
 * <p>本类已被 Spring 托管（@Component），直接在其他类中注入即可使用：</p>
 * <pre>
 *   {@code @Autowired}
 *   private JwtUtils jwtUtils;
 * </pre>
 *
 * ===================== 使用示例 =====================
 *
 * 【1】登录成功后 —— 生成 Token
 * <pre>
 *   // 仅携带用户名
 *   String token = jwtUtils.generateToken("admin");
 *
 *   // 携带额外信息（如角色）
 *   Map<String, Object> claims = new HashMap<>();
 *   claims.put("role", "ADMIN");
 *   String token = jwtUtils.generateToken("admin", claims);
 * </pre>
 *
 * 【2】请求接口时 —— 验证 Token（通常在过滤器或拦截器中使用）
 * <pre>
 *   // 从请求头获取 Token（前端传入格式：Authorization: Bearer xxxxx）
 *   String authHeader = request.getHeader("Authorization");
 *   String token = authHeader.substring(7); // 去掉 "Bearer " 前缀
 *
 *   // 从 Token 中拿到用户名
 *   String username = jwtUtils.getUsername(token);
 *
 *   // 验证 Token 是否合法（签名正确 + 未过期 + 用户名匹配）
 *   boolean valid = jwtUtils.validateToken(token, username);
 * </pre>
 *
 * 【3】获取 Token 中的自定义信息
 * <pre>
 *   // 获取角色
 *   String role = (String) jwtUtils.getClaim(token, "role");
 *
 *   // 获取过期时间
 *   Date expiration = jwtUtils.getExpiration(token);
 *
 *   // 判断是否过期
 *   boolean expired = jwtUtils.isExpired(token);
 * </pre>
 *
 * 【4】Token 即将过期时 —— 刷新 Token
 * <pre>
 *   String newToken = jwtUtils.refreshToken(oldToken);
 *   // 将 newToken 返回给前端，前端替换本地存储的旧 Token
 * </pre>
 *
 * ===================== 注意事项 =====================
 * 1. SECRET 密钥长度至少 32 个字符（256位），生产环境请移至 application.properties：
 *       jwt.secret=your-secret-key
 *    并通过 @Value("${jwt.secret}") 注入。
 * 2. Token 默认有效期为 7 天，可修改 EXPIRATION_MS 常量调整。
 * 3. 前端请求时须在 Header 中携带：Authorization: Bearer <token>
 */
@Component
public class JwtUtils {

    // 密钥（至少256位，生产环境请放到配置文件中）
    private static final String SECRET = "ToDoApplication-jwt-secret-key-2026!!";
    // Token 有效期：7天
    private static final long EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;

    private final SecretKey secretKey;

    public JwtUtils() {
        this.secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 Token（只携带用户名）
     *
     * @param username 用户名
     * @return JWT 字符串
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 生成 Token（携带自定义 claims）
     *
     * @param username 用户名
     * @param claims   额外的 claims，如角色、权限等
     * @return JWT 字符串
     */
    public String generateToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从 Token 中解析所有 Claims
     *
     * @param token JWT 字符串
     * @return Claims 对象
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中获取用户名（subject）
     *
     * @param token JWT 字符串
     * @return 用户名
     */
    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * 从 Token 中获取指定 claim 的值
     *
     * @param token    JWT 字符串
     * @param claimKey claim 键名
     * @return claim 值（Object类型，自行强转）
     */
    public Object getClaim(String token, String claimKey) {
        return parseToken(token).get(claimKey);
    }

    /**
     * 获取 Token 的过期时间
     *
     * @param token JWT 字符串
     * @return 过期时间
     */
    public Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    /**
     * 判断 Token 是否已过期
     *
     * @param token JWT 字符串
     * @return true 表示已过期
     */
    public boolean isExpired(String token) {
        try {
            return getExpiration(token).before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    /**
     * 验证 Token 是否合法（签名正确且未过期）
     *
     * @param token    JWT 字符串
     * @param username 期望的用户名
     * @return true 表示合法
     */
    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsername(token);
            return tokenUsername.equals(username) && !isExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 刷新 Token（重新签发，重置过期时间）
     *
     * @param token 旧 Token
     * @return 新 Token
     */
    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        return Jwts.builder()
                .subject(claims.getSubject())
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }
}

