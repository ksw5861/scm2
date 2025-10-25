package com.yedam.scm.web;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RedisTestController {
    private final StringRedisTemplate redisTemplate;

    @GetMapping("/redis/set")
    public String setValue() {
        redisTemplate.opsForValue().set("testKey", "HelloRedis");
        return "Saved";
    }

    @GetMapping("/redis/get")
    public String getValue() {
        return redisTemplate.opsForValue().get("testKey");
    }
}
