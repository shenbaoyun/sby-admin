package com.sby.project.login.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;

public class JwtUtils {
    /**
     * 签名密钥：用于对 JWT 进行数字签名的秘钥，防止 Token 内容被篡改。
     * 这里的密钥应妥善保管，生产环境建议从配置文件中读取。
     */
    private static final String SIGN_KEY = "sby_project_2026_secret";

    /**
     * 生成 JWT 令牌
     * 【新增】支持自定义过期时间的方法
     * @param claims claims 载荷数据（Payload），通常包含用户 ID、用户名、权限等非敏感信息
     * @param expireTime 过期时间（毫秒）
     * @return 生成的 JWT 字符串
     */
    public static String createToken(Map<String, Object> claims, Long expireTime) {
        return Jwts.builder()
                .addClaims(claims)                                                  // 设置自定义负载信息
                .signWith(SignatureAlgorithm.HS256, SIGN_KEY)                       // 设置签名算法及密钥
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))   // 设置过期时间点
                .compact();                                                         // 压缩生成最终的字符串
    }


    /**
     * 解析并校验 JWT 令牌
     * * @param token 客户端传回的 JWT 字符串
     * @return 解析后的声明集（Claims），从中可以获取之前存入的数据
     * @throws io.jsonwebtoken.JwtException 如果 Token 已过期、被篡改或格式错误，会抛出异常
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SIGN_KEY) // 指定解密和校验时使用的密钥
                .parseClaimsJws(token)   // 解析 JWS（带签名的 JWT）
                .getBody();              // 获取 Payload 负载部分
    }
}