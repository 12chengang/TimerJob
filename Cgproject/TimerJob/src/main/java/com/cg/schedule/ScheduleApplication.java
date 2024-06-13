package com.cg.schedule;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.cg"})
@EnableScheduling
@EnableAsync
public class ScheduleApplication {

    public static void main(String[] args) {


        ConfigurableApplicationContext run = SpringApplication.run(ScheduleApplication.class, args);

    }

}
