package ru.senya.sampleserv;

import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SampleServApplication {

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("Version: " + Core.VERSION);
        } catch (UnsatisfiedLinkError ignored) {
        }
    }

    public static void main(String[] args) {
        try {
            SpringApplication.run(SampleServApplication.class, args);
        } catch (Exception e){
            main(args);
        }
    }

}
