package com.sby.project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class SbyServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void testGenPassword() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // 生成 123456 的加密串
        System.out.println(encoder.encode("123456"));
    }

}
