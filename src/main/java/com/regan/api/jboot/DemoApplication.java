package com.regan.api.jboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        System.out.println("\n" +
                "_ __ ___  __ _  __ _ _ __     __ _ _ __  (_)\n" +
                "| '__/ _ \\/ _` |/ _` | '_ \\   / _` | '_ \\| |\n" +
                "| | |  __/ (_| | (_| | | | | | (_| | |_) | |\n" +
                "|_|  \\___|\\__, |\\__,_|_| |_|  \\__,_| .__/|_|\n" +
                "|___/       _| |         |_|       |_|\n" +
                "            \\__|\n" +
                "            \n" +
                "               启 动 成 功");
    }
}
