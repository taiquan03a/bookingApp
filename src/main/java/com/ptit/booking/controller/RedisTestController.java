package com.ptit.booking.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis")
public class RedisTestController {
    private final StringRedisTemplate redisTemplate;

    public RedisTestController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/test")
    public String testRedis() {
        redisTemplate.opsForValue().set("testKey", "Hello Redis!");
        return redisTemplate.opsForValue().get("testKey");
    }
}