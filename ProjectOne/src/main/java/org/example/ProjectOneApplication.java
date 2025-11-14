package org.example;

import org.example.controller.Dev;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class
ProjectOneApplication {
    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ProjectOneApplication.class, args);

        Dev obj = context.getBean(Dev.class);

        obj.build();
        // Without Dependency injection
//        Dev obj = new Dev();
//        obj.build();
    }
}
