package com.github.freegeese.easymybatis.test;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.github.freegeese.easymybatis"})
public class EasyMybatisTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyMybatisTestApplication.class, args);
    }
}
