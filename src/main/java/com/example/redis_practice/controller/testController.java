package com.example.redis_practice.controller;

import com.example.redis_practice.service.SeckillService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/")
public class testController {

    private final SeckillService seckillService;

    public testController(SeckillService seckillService) {
        this.seckillService = seckillService;
    }

    @GetMapping("/seckill/{prodId}")
    public String test(@PathVariable("prodId")String prodId) {
        String userId = new Random().nextInt(50000) + "";
        return "秒杀结果："+seckillService.doSeckill(userId, prodId);
    }

}
