package com.zhunong.mall.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    /**
     * JWT密钥
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * 过期时间（毫秒）
     */
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    /**
     * 生成JWT令牌
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色
     * @return JWT令牌
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        return JWT.create()
                .withSubject(String.valueOf(userId))
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", role)
                .withIssuedAt(now)
                .withExpiresAt(expireDate)
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 验证JWT令牌
     *
     * @param token JWT令牌
     * @return 验证成功返回DecodedJWT，失败返回null
     */
    public DecodedJWT verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
            return verifier.verify(token);
        } catch (JWTVerificationException e) {
            log.error("JWT验证失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从令牌中获取用户ID
     *
     * @param token JWT令牌
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("userId").asLong();
        } catch (JWTDecodeException e) {
            log.error("解析JWT失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token JWT令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("username").asString();
        } catch (JWTDecodeException e) {
            log.error("解析JWT失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从令牌中获取角色
     *
     * @param token JWT令牌
     * @return 角色
     */
    public String getRoleFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("role").asString();
        } catch (JWTDecodeException e) {
            log.error("解析JWT失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 判断令牌是否过期
     *
     * @param token JWT令牌
     * @return true-已过期，false-未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (JWTDecodeException e) {
            log.error("解析JWT失败: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 刷新令牌
     *
     * @param token 旧令牌
     * @return 新令牌
     */
    public String refreshToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        if (jwt == null) {
            return null;
        }
        Long userId = jwt.getClaim("userId").asLong();
        String username = jwt.getClaim("username").asString();
        String role = jwt.getClaim("role").asString();
        return generateToken(userId, username, role);
    }
}
