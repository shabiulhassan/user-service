package dev.deepak.userservicetestfinal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class UserservicetestfinalApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserservicetestfinalApplication.class, args);
    }

}
