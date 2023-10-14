package blog.yrol;

import blog.yrol.shared.SpringApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class UserServicesRest implements CommandLineRunner {

    public static final Logger LOG = LoggerFactory.getLogger(UserServicesRest.class);

    public static void main(String[] args) {
        SpringApplication.run(UserServicesRest.class);
    }


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SpringApplicationContext springApplicationContext() {
        return new SpringApplicationContext();
    }


    @Override
    public void run(String... args) throws Exception {
        LOG.info("User Services Rest started");
    }
}