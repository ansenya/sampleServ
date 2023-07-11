package ru.senya.sampleserv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import static ru.senya.sampleserv.utils.Utils.init;

@SpringBootApplication
public class SampleServApplication {
    public static void main(String[] args) {
        init();
        SpringApplication.run(SampleServApplication.class, args);
    }
}
