package com.jc.integralgradle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

@ImportResource("classpath:applicationContext.xml")
@SpringBootApplication
public class IntegralgradleApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntegralgradleApplication.class, args);
    }

}
