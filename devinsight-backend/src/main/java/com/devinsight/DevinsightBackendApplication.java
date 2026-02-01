package com.devinsight;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.devinsight.mapper")
public class DevinsightBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevinsightBackendApplication.class, args);
    }

}
